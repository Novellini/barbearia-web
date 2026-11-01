package br.com.barbearia.web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico")
    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100)
    private String nome;

    private String descricao;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço não pode ser negativo")
    private BigDecimal preco;

    @NotNull(message = "Duração é obrigatória")
    @Min(value = 5, message = "Duração mínima de 5 minutos")
    @Max(value = 480, message = "Duração máxima de 480 minutos")
    @Column(name = "duracao_min")
    private Integer duracaoMin = 30;

    private boolean ativo = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String d) { this.descricao = d == null || d.isBlank() ? null : d.trim(); }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Integer getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(Integer duracaoMin) { this.duracaoMin = duracaoMin; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
