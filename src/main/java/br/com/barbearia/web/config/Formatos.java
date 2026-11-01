package br.com.barbearia.web.config;

import br.com.barbearia.web.service.AgendamentoService;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

// Usado nos templates como ${@fmt.status(...)}, ${@fmt.moeda(...)} etc.
@Component("fmt")
public class Formatos {

    private static final Locale BR = Locale.of("pt", "BR");

    public String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(BR).format(valor != null ? valor : BigDecimal.ZERO);
    }

    public String status(String status) {
        if (status == null || status.isBlank()) return "-";
        return switch (status.toLowerCase()) {
            case "concluido" -> "Concluído";
            default -> Character.toUpperCase(status.charAt(0)) + status.substring(1).toLowerCase();
        };
    }

    // Classe CSS da etiqueta de status
    public String cor(String status) {
        if (status == null) return "neutro";
        return switch (status.toLowerCase()) {
            case "confirmado", "concluido", "pago" -> "sucesso";
            case "cancelado" -> "perigo";
            default -> "alerta";
        };
    }

    public String forma(String forma) {
        return AgendamentoService.FORMAS_PAGAMENTO.getOrDefault(forma, forma);
    }
}
