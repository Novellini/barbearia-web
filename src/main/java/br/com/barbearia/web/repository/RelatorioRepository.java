package br.com.barbearia.web.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Repository
public class RelatorioRepository {
    private final javax.sql.DataSource dataSource;

    public RelatorioRepository(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // A agenda usa a data do atendimento. O caixa usa a data do pagamento.
    private static final String FLUXO = """
            SELECT dia, SUM(agendados) AS agendados, SUM(concluidos) AS concluidos,
                   SUM(cancelados) AS cancelados, SUM(recebido) AS recebido
            FROM (
                SELECT DATE(data_hora) AS dia, COUNT(*) AS agendados,
                       SUM(status = 'concluido') AS concluidos,
                       SUM(status = 'cancelado') AS cancelados, 0 AS recebido
                FROM agendamento WHERE data_hora >= ? AND data_hora < ?
                GROUP BY DATE(data_hora)
                UNION ALL
                SELECT DATE(pago_em), 0, 0, 0, SUM(valor_total)
                FROM pagamento WHERE status = 'pago' AND pago_em >= ? AND pago_em < ?
                GROUP BY DATE(pago_em)
            ) AS movimento
            GROUP BY dia ORDER BY dia
            """;

    // O pagamento entra uma vez, mesmo que o agendamento tenha vários serviços.
    private static final String BARBEIROS = """
            SELECT b.nome, SUM(COALESCE(i.quantidade, 0)) AS quantidade,
                   COUNT(*) AS atendimentos, SUM(p.valor_total) AS recebido
            FROM pagamento p
            JOIN agendamento a ON a.id_agendamento = p.id_agendamento
            JOIN barbeiro b ON b.id_barbeiro = a.id_barbeiro
            LEFT JOIN (
                SELECT id_agendamento, COUNT(*) AS quantidade
                FROM agendamento_servico GROUP BY id_agendamento
            ) i ON i.id_agendamento = a.id_agendamento
            WHERE p.status = 'pago' AND p.pago_em >= ? AND p.pago_em < ?
            GROUP BY b.id_barbeiro, b.nome
            ORDER BY quantidade DESC, recebido DESC, b.nome, b.id_barbeiro
            """;

    private static final String SERVICOS = """
            SELECT s.nome, COUNT(*) AS quantidade, SUM(i.preco_cobrado) AS valor
            FROM pagamento p
            JOIN agendamento_servico i ON i.id_agendamento = p.id_agendamento
            JOIN servico s ON s.id_servico = i.id_servico
            WHERE p.status = 'pago' AND p.pago_em >= ? AND p.pago_em < ?
            GROUP BY s.id_servico, s.nome
            ORDER BY quantidade DESC, valor DESC, s.nome, s.id_servico
            """;

    public record Dia(LocalDate data, long agendados, long concluidos, long cancelados, BigDecimal recebido) {}
    public record Barbeiro(String nome, long quantidade, long atendimentos, BigDecimal recebido) {}
    public record Servico(String nome, long quantidade, BigDecimal valor) {}
    public record Relatorio(List<Dia> dias, List<Barbeiro> barbeiros, List<Servico> servicos) {
        public long agendados() { return dias.stream().mapToLong(Dia::agendados).sum(); }
        public long concluidos() { return dias.stream().mapToLong(Dia::concluidos).sum(); }
        public long cancelados() { return dias.stream().mapToLong(Dia::cancelados).sum(); }
        public BigDecimal recebido() {
            return dias.stream().map(Dia::recebido).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public Relatorio consultar(LocalDate inicio, LocalDate fim) throws SQLException {
        LocalDate limite = fim.plusDays(1);
        List<Dia> dias = new ArrayList<>();
        List<Barbeiro> barbeiros = new ArrayList<>();
        List<Servico> servicos = new ArrayList<>();
        try (Connection con = dataSource.getConnection()) {
            // As três consultas precisam ler os mesmos dados, mesmo se a agenda mudar.
            con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            con.setReadOnly(true);
            con.setAutoCommit(false);
            try (var ps = periodo(con, FLUXO, inicio, limite, 2); var rs = ps.executeQuery()) {
                while (rs.next()) {
                    dias.add(new Dia(rs.getObject("dia", LocalDate.class), rs.getLong("agendados"),
                            rs.getLong("concluidos"), rs.getLong("cancelados"), rs.getBigDecimal("recebido")));
                }
            }
            try (var ps = periodo(con, BARBEIROS, inicio, limite, 1); var rs = ps.executeQuery()) {
                while (rs.next()) {
                    barbeiros.add(new Barbeiro(rs.getString("nome"), rs.getLong("quantidade"),
                            rs.getLong("atendimentos"), rs.getBigDecimal("recebido")));
                }
            }
            try (var ps = periodo(con, SERVICOS, inicio, limite, 1); var rs = ps.executeQuery()) {
                while (rs.next()) {
                    servicos.add(new Servico(rs.getString("nome"), rs.getLong("quantidade"), rs.getBigDecimal("valor")));
                }
            }
            con.commit();
        }
        return new Relatorio(List.copyOf(dias), List.copyOf(barbeiros), List.copyOf(servicos));
    }

    private PreparedStatement periodo(Connection con, String sql, LocalDate inicio,
                                      LocalDate limite, int intervalos) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        try {
            for (int i = 0; i < intervalos; i++) {
                ps.setObject(i * 2 + 1, inicio.atStartOfDay());
                ps.setObject(i * 2 + 2, limite.atStartOfDay());
            }
            return ps;
        } catch (SQLException ex) {
            ps.close();
            throw ex;
        }
    }
}
