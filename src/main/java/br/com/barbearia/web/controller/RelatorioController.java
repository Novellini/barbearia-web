package br.com.barbearia.web.controller;

import br.com.barbearia.web.repository.RelatorioRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RelatorioController {
    private final RelatorioRepository repositorio;

    public RelatorioController(RelatorioRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping("/relatorios")
    public String relatorios(@RequestParam(required = false) String inicio,
                             @RequestParam(required = false) String fim, Model model) throws SQLException {
        LocalDate hoje = LocalDate.now();
        String inicioInformado = inicio == null ? hoje.withDayOfMonth(1).toString() : inicio;
        String fimInformado = fim == null ? hoje.toString() : fim;
        model.addAttribute("inicio", inicioInformado);
        model.addAttribute("fim", fimInformado);
        try {
            LocalDate de = LocalDate.parse(inicioInformado);
            LocalDate ate = LocalDate.parse(fimInformado);
            if (de.isBefore(LocalDate.of(1000, 1, 1)) || ate.getYear() > 9998
                    || de.isAfter(ate) || ate.isAfter(de.plusDays(365))) {
                model.addAttribute("erro", "Use um intervalo válido de até 366 dias.");
            } else {
                model.addAttribute("relatorio", repositorio.consultar(de, ate));
            }
        } catch (DateTimeParseException ex) {
            model.addAttribute("erro", "Informe as datas de início e fim.");
        }
        return "relatorios";
    }
}
