package br.com.barbearia.web.controller;

import br.com.barbearia.web.model.Agendamento;
import br.com.barbearia.web.repository.BarbeiroRepository;
import br.com.barbearia.web.repository.ClienteRepository;
import br.com.barbearia.web.repository.ServicoRepository;
import br.com.barbearia.web.service.AgendamentoService;
import br.com.barbearia.web.service.NovoAgendamentoForm;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agenda")
public class AgendaController {

    private final AgendamentoService service;
    private final ClienteRepository clientes;
    private final BarbeiroRepository barbeiros;
    private final ServicoRepository servicos;

    public AgendaController(AgendamentoService service, ClienteRepository clientes,
                            BarbeiroRepository barbeiros, ServicoRepository servicos) {
        this.service = service;
        this.clientes = clientes;
        this.barbeiros = barbeiros;
        this.servicos = servicos;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                        Model model) {
        LocalDate dia = data != null ? data : LocalDate.now();
        model.addAttribute("data", dia);
        model.addAttribute("hoje", dia.equals(LocalDate.now()));
        model.addAttribute("agendamentos", service.doDia(dia));
        return "agenda/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                       Model model) {
        NovoAgendamentoForm form = new NovoAgendamentoForm();
        if (data != null && !data.isBefore(LocalDate.now())) form.setData(data);
        return formulario(form, model);
    }

    @PostMapping("/novo")
    public String criar(@Valid @ModelAttribute("form") NovoAgendamentoForm form, BindingResult erros,
                        Model model, RedirectAttributes redirect) {
        if (erros.hasErrors()) return formulario(form, model);
        try {
            Agendamento a = service.criar(form);
            redirect.addFlashAttribute("sucesso", "Agendamento confirmado para " + a.getCliente().getNome() + ".");
            return "redirect:/agenda?data=" + form.getData();
        } catch (IllegalArgumentException ex) {
            erros.reject("agendamento", ex.getMessage());
            return formulario(form, model);
        }
    }

    private String formulario(NovoAgendamentoForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("clientes", clientes.findAllByOrderByNome());
        model.addAttribute("barbeiros", barbeiros.findByAtivoTrueOrderByNome());
        model.addAttribute("servicos", servicos.findByAtivoTrueOrderByNome());
        model.addAttribute("horarios", service.horariosLivres(form.getBarbeiroId(), form.getData(), form.getServicoIds()));
        return "agenda/novo";
    }

    // Usado pelo formulário para atualizar os horários sem recarregar a página
    @GetMapping("/horarios")
    @ResponseBody
    public List<String> horarios(@RequestParam(required = false) Integer barbeiroId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                                 @RequestParam(required = false) List<Integer> servicoIds) {
        return service.horariosLivres(barbeiroId, data, servicoIds);
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Integer id, Model model) {
        model.addAttribute("ag", service.buscar(id));
        return "agenda/detalhes";
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable Integer id, @RequestParam String status, RedirectAttributes redirect) {
        try {
            service.mudarStatus(id, status);
            redirect.addFlashAttribute("sucesso", "Status atualizado.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/agenda/" + id;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirect) {
        LocalDate data = service.buscar(id).getDataHora().toLocalDate();
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Agendamento excluído.");
        return "redirect:/agenda?data=" + data;
    }
}
