package br.com.barbearia.web.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

// Tabela intermediária agendamento_servico (guarda o preço cobrado na época)
@Entity
@Table(name = "agendamento_servico")
public class AgendamentoServico {

    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "id_agendamento")
        private Integer idAgendamento;
        @Column(name = "id_servico")
        private Integer idServico;

        public Id() {}
        public Id(Integer idAgendamento, Integer idServico) {
            this.idAgendamento = idAgendamento;
            this.idServico = idServico;
        }

        @Override public boolean equals(Object o) {
            return o instanceof Id i && Objects.equals(idAgendamento, i.idAgendamento) && Objects.equals(idServico, i.idServico);
        }
        @Override public int hashCode() { return Objects.hash(idAgendamento, idServico); }
    }

    @EmbeddedId
    private Id id = new Id();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAgendamento")
    @JoinColumn(name = "id_agendamento")
    private Agendamento agendamento;

    @ManyToOne
    @MapsId("idServico")
    @JoinColumn(name = "id_servico")
    private Servico servico;

    @Column(name = "preco_cobrado")
    private BigDecimal precoCobrado;

    protected AgendamentoServico() {}

    public AgendamentoServico(Agendamento agendamento, Servico servico) {
        this.agendamento = agendamento;
        this.servico = servico;
        this.precoCobrado = servico.getPreco();
    }

    public Servico getServico() { return servico; }
    public BigDecimal getPrecoCobrado() { return precoCobrado; }
}
