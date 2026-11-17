package br.com.barbearia.web.service;

import br.com.barbearia.web.model.*;
import br.com.barbearia.web.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendamentoService {

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

    @Transactional(readOnly = true)
    public List<String> horariosLivres(Integer idBarbeiro, LocalDate data, List<Integer> idsServicos) {
        if (idBarbeiro == null || data == null || data.isBefore(LocalDate.now()) || data.getYear() > 9998
                || barbeiros.findById(idBarbeiro).filter(Barbeiro::isAtivo).isEmpty()) return List.of();
        try {
            return GradeHorarios.livres(data, duracao(selecionarServicos(idsServicos)), reservas(idBarbeiro, data));
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }

    private List<Servico> selecionarServicos(List<Integer> ids) {
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(Objects::isNull) || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Selecione serviços válidos.");
        }
        List<Servico> selecionados = servicos.findAllById(ids);
        if (selecionados.size() != ids.size()
                || selecionados.stream().anyMatch(s -> !s.isAtivo() || s.getDuracaoMin() <= 0)) {
            throw new IllegalArgumentException("Um dos serviços selecionados não está disponível.");
        }
        return selecionados;
    }

    private int duracao(List<Servico> selecionados) {
        return selecionados.stream().mapToInt(Servico::getDuracaoMin).sum();
    }

    private List<GradeHorarios.Reserva> reservas(Integer barbeiro, LocalDate data) {
        return agendamentos.buscarReservas(barbeiro, data.atStartOfDay(), data.plusDays(1).atStartOfDay())
                .stream().map(a -> new GradeHorarios.Reserva(a.getDataHora(), a.minutosReservados())).toList();
    }

    @Transactional
    public Agendamento criar(NovoAgendamentoForm form) {
        if (form.getData().getYear() > 9998) throw new IllegalArgumentException("Informe uma data válida.");
        // O desktop também bloqueia esta linha antes de consultar e gravar.
        Barbeiro barbeiro = barbeiros.bloquearParaAgendar(form.getBarbeiroId()).filter(Barbeiro::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Selecione um barbeiro ativo."));
        List<Servico> selecionados = selecionarServicos(form.getServicoIds());
        int minutos = duracao(selecionados);
        LocalDateTime inicio = LocalDateTime.of(form.getData(), form.getHorario());
        if (!GradeHorarios.cabe(inicio, minutos, LocalDateTime.now(), reservas(barbeiro.getId(), form.getData()))) {
            throw new IllegalArgumentException("O atendimento não cabe nesse horário. Escolha outro.");
        }
        Agendamento a = new Agendamento();
        a.setCliente(clientes.findById(form.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado")));
        a.setBarbeiro(barbeiro);
        a.setDataHora(inicio);
        a.setDuracaoReservada(minutos);
        a.setObservacao(form.getObservacao());
        for (Servico s : selecionados) a.adicionarServico(s);
        return agendamentos.save(a);
    }

    private Agendamento buscarParaAlterar(Integer id) {
        Agendamento a = agendamentos.bloquearPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
        if (Agendamento.CANCELADO.equals(a.getStatus())) {
            throw new IllegalArgumentException("Este agendamento foi cancelado.");
        }
        return a;
    }

    @Transactional
    public void mudarStatus(Integer id, String status) {
        if (!List.of(Agendamento.CONFIRMADO, Agendamento.CONCLUIDO, Agendamento.CANCELADO).contains(status)) {
            throw new IllegalArgumentException("Status inválido");
        }
        buscarParaAlterar(id).setStatus(status);
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
        Agendamento a = buscarParaAlterar(idAgendamento);
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
