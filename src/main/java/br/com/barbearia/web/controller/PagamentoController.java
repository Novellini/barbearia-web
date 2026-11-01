package br.com.barbearia.web.controller;

import br.com.barbearia.web.model.Agendamento;
import br.com.barbearia.web.service.AgendamentoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Tela 6 do wireframe: registrar pagamento
@Controller
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final AgendamentoService service;

    public PagamentoController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping
    public String form(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                       @RequestParam(required = false) Integer agendamento, Model model) {
        LocalDate dia = data != null ? data : LocalDate.now();
        List<Agendamento> pendentes = service.pendentesDePagamento(dia);
        Agendamento selecionado = pendentes.stream()
                .filter(a -> a.getId().equals(agendamento)).findFirst()
                .orElse(pendentes.isEmpty() ? null : pendentes.get(0));
        model.addAttribute("data", dia);
        model.addAttribute("pendentes", pendentes);
        model.addAttribute("selecionado", selecionado);
        model.addAttribute("formas", AgendamentoService.FORMAS_PAGAMENTO);
        return "pagamentos/form";
    }

    @PostMapping
    public String registrar(@RequestParam Integer agendamento, @RequestParam(required = false) String valor,
                            @RequestParam(required = false) String forma, @RequestParam String status,
                            RedirectAttributes redirect) {
        LocalDate data = service.buscar(agendamento).getDataHora().toLocalDate();
        try {
            BigDecimal v = new BigDecimal(valor.replace("R$", "").trim().replace(".", "").replace(",", "."));
            service.registrarPagamento(agendamento, v, forma, "pago".equals(status));
            redirect.addFlashAttribute("sucesso", "pago".equals(status)
                    ? "Pagamento registrado e atendimento concluído." : "Pagamento registrado como pendente.");
            return "redirect:/pagamentos?data=" + data;
        } catch (NumberFormatException | NullPointerException ex) {
            redirect.addFlashAttribute("erro", "Valor inválido. Ex.: 50,00");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/pagamentos?data=" + data + "&agendamento=" + agendamento;
    }
}
