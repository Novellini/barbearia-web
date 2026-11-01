package br.com.barbearia.web.service;

import br.com.barbearia.web.model.*;
import br.com.barbearia.web.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendamentoService {

    // Mesma grade de horários do desktop: 08:00 às 19:30, de 30 em 30 minutos
    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(20, 0);
    private static final int INTERVALO_MIN = 30;

    public static final Map<String, String> FORMAS_PAGAMENTO = new LinkedHashMap<>();
    static {
        FORMAS_PAGAMENTO.put("dinheiro", "Dinheiro");
        FORMAS_PAGAMENTO.put("pix", "PIX");
        FORMAS_PAGAMENTO.put("cartao_debito", "Cartão débito");
        FORMAS_PAGAMENTO.put("cartao_credito", "Cartão crédito");
    }

    private final AgendamentoRepository agendamentos;
    private final ClienteRepository clientes;
    private final BarbeiroRepository barbeiros;
    private final ServicoRepository servicos;
    private final PagamentoRepository pagamentos;

    public AgendamentoService(AgendamentoRepository agendamentos, ClienteRepository clientes,
                              BarbeiroRepository barbeiros, ServicoRepository servicos,
                              PagamentoRepository pagamentos) {
        this.agendamentos = agendamentos;
        this.clientes = clientes;
        this.barbeiros = barbeiros;
        this.servicos = servicos;
        this.pagamentos = pagamentos;
    }

    public List<Agendamento> doDia(LocalDate data) {
        return agendamentos.findByDataHoraBetweenOrderByDataHora(data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }

    public long contarDoDia(LocalDate data) {
        return agendamentos.countByDataHoraBetweenAndStatusNot(data.atStartOfDay(), data.plusDays(1).atStartOfDay(), Agendamento.CANCELADO);
    }

    public long contarConcluidos(LocalDate data) {
        return agendamentos.countByDataHoraBetweenAndStatus(data.atStartOfDay(), data.plusDays(1).atStartOfDay(), Agendamento.CONCLUIDO);
    }

    // Próximos atendimentos de hoje que ainda não começaram
    public List<Agendamento> proximos(int limite) {
        LocalDateTime agora = LocalDateTime.now();
        return doDia(LocalDate.now()).stream()
                .filter(a -> !a.isEncerrado() && !a.getDataHora().isBefore(agora))
                .limit(limite).toList();
    }

    public Agendamento buscar(Integer id) {
        return agendamentos.findById(id).orElseThrow(() -> new NoSuchElementException("Agendamento não encontrado"));
    }

    public List<String> horariosLivres(Integer idBarbeiro, LocalDate data) {
        if (idBarbeiro == null || data == null) return List.of();
        Set<LocalTime> ocupados = new HashSet<>();
        for (Agendamento a : agendamentos.findByBarbeiroIdAndDataHoraBetween(idBarbeiro,
                data.atStartOfDay(), data.plusDays(1).atStartOfDay())) {
            ocupados.add(a.getDataHora().toLocalTime());
        }
        LocalDateTime agora = LocalDateTime.now();
        List<String> livres = new ArrayList<>();
        for (LocalTime h = ABERTURA; h.isBefore(FECHAMENTO); h = h.plusMinutes(INTERVALO_MIN)) {
            if (!ocupados.contains(h) && !LocalDateTime.of(data, h).isBefore(agora)) livres.add(h.toString());
        }
        return livres;
    }

    // Lança IllegalArgumentException com a mensagem para o usuário quando algo não é válido
    @Transactional
    public Agendamento criar(NovoAgendamentoForm form) {
        LocalDateTime dataHora = LocalDateTime.of(form.getData(), form.getHorario());
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Esse horário já passou. Escolha outro.");
        }
        if (agendamentos.existsByBarbeiroIdAndDataHora(form.getBarbeiroId(), dataHora)) {
            throw new IllegalArgumentException("Esse barbeiro já tem um agendamento neste horário. Escolha outro.");
        }
        Agendamento a = new Agendamento();
        a.setCliente(clientes.findById(form.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado")));
        a.setBarbeiro(barbeiros.findById(form.getBarbeiroId())
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado")));
        a.setDataHora(dataHora);
        a.setObservacao(form.getObservacao());
        for (Servico s : servicos.findAllById(form.getServicoIds())) a.adicionarServico(s);
        return agendamentos.save(a);
    }

    @Transactional
    public void mudarStatus(Integer id, String status) {
        if (!List.of(Agendamento.CONFIRMADO, Agendamento.CONCLUIDO, Agendamento.CANCELADO).contains(status)) {
            throw new IllegalArgumentException("Status inválido");
        }
        buscar(id).setStatus(status);
    }

    @Transactional
    public void excluir(Integer id) {
        agendamentos.delete(buscar(id));
    }

    // Atendimentos da data que ainda não foram pagos nem cancelados
    public List<Agendamento> pendentesDePagamento(LocalDate data) {
        return doDia(data).stream()
                .filter(a -> !a.isPago() && !Agendamento.CANCELADO.equals(a.getStatus()))
                .toList();
    }

    // Se já existir pagamento pendente para o agendamento, ele é atualizado
    @Transactional
    public void registrarPagamento(Integer idAgendamento, BigDecimal valor, String forma, boolean pago) {
        if (!FORMAS_PAGAMENTO.containsKey(forma)) throw new IllegalArgumentException("Escolha a forma de pagamento");
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("Informe um valor maior que zero");
        Agendamento a = buscar(idAgendamento);
        if (a.isPago()) throw new IllegalArgumentException("Esse atendimento já está pago");
        Pagamento p = pagamentos.findByAgendamentoId(idAgendamento).orElseGet(Pagamento::new);
        p.setAgendamento(a);
        p.setValorTotal(valor);
        p.setFormaPagamento(forma);
        p.setStatus(pago ? "pago" : "pendente");
        p.setPagoEm(LocalDateTime.now());
        pagamentos.save(p);
        if (pago) a.setStatus(Agendamento.CONCLUIDO);
    }
}
