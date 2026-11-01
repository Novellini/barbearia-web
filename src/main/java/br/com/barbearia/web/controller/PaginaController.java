package br.com.barbearia.web.controller;

import br.com.barbearia.web.repository.ClienteRepository;
import br.com.barbearia.web.service.AgendamentoService;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    private final AgendamentoService agendamentoService;
    private final ClienteRepository clientes;

    public PaginaController(AgendamentoService agendamentoService, ClienteRepository clientes) {
        this.agendamentoService = agendamentoService;
        this.clientes = clientes;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Tela 2 do wireframe: resumo do dia
    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate hoje = LocalDate.now();
        model.addAttribute("totalAgendamentos", agendamentoService.contarDoDia(hoje));
        model.addAttribute("totalClientes", clientes.count());
        model.addAttribute("totalConcluidos", agendamentoService.contarConcluidos(hoje));
        model.addAttribute("proximos", agendamentoService.proximos(5));
        return "dashboard";
    }
}
