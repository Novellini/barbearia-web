package br.com.barbearia.web.service;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

// Dados do formulário "Novo agendamento"
public class NovoAgendamentoForm {

    @NotNull(message = "Selecione um cliente")
    private Integer clienteId;

    @NotNull(message = "Selecione um barbeiro")
    private Integer barbeiroId;

    @NotEmpty(message = "Selecione pelo menos um serviço")
    private List<Integer> servicoIds = new ArrayList<>();

    @NotNull(message = "Informe a data")
    @FutureOrPresent(message = "Não é possível agendar em uma data passada")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data = LocalDate.now();

    @NotNull(message = "Selecione um horário disponível")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horario;

    @Size(max = 500)
    private String observacao;

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public Integer getBarbeiroId() { return barbeiroId; }
    public void setBarbeiroId(Integer barbeiroId) { this.barbeiroId = barbeiroId; }
    public List<Integer> getServicoIds() { return servicoIds; }
    public void setServicoIds(List<Integer> servicoIds) { this.servicoIds = servicoIds; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
