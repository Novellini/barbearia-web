package br.com.barbearia.web.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    // Mesmos valores gravados pelo sistema desktop
    public static final String AGENDADO = "agendado";
    public static final String CONFIRMADO = "confirmado";
    public static final String CONCLUIDO = "concluido";
    public static final String CANCELADO = "cancelado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agendamento")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_barbeiro")
    private Barbeiro barbeiro;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    private String status = AGENDADO;

    private String observacao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgendamentoServico> itens = new ArrayList<>();

    @OneToOne(mappedBy = "agendamento", cascade = CascadeType.REMOVE)
    private Pagamento pagamento;

    public void adicionarServico(Servico s) { itens.add(new AgendamentoServico(this, s)); }

    public BigDecimal getTotal() {
        return itens.stream().map(AgendamentoServico::getPrecoCobrado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getNomesServicos() {
        return itens.stream().map(i -> i.getServico().getNome()).collect(Collectors.joining(" + "));
    }

    public int getDuracaoTotal() {
        return itens.stream().mapToInt(i -> i.getServico().getDuracaoMin()).sum();
    }

    public boolean isPago() { return pagamento != null && "pago".equalsIgnoreCase(pagamento.getStatus()); }

    public boolean isEncerrado() { return CONCLUIDO.equals(status) || CANCELADO.equals(status); }

    public Integer getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Barbeiro getBarbeiro() { return barbeiro; }
    public void setBarbeiro(Barbeiro barbeiro) { this.barbeiro = barbeiro; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getStatus() { return status == null ? AGENDADO : status.toLowerCase(); }
    public void setStatus(String status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String o) { this.observacao = o == null || o.isBlank() ? null : o.trim(); }
    public List<AgendamentoServico> getItens() { return itens; }
    public Pagamento getPagamento() { return pagamento; }
}
