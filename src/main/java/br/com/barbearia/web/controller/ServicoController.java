package br.com.barbearia.web.controller;

import br.com.barbearia.web.model.Servico;
import br.com.barbearia.web.repository.ServicoRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoRepository repo;

    public ServicoController(ServicoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String busca, Model model) {
        boolean filtrando = busca != null && !busca.isBlank();
        model.addAttribute("servicos", filtrando ? repo.buscar(busca.trim()) : repo.findAllByOrderByNome());
        model.addAttribute("busca", busca);
        model.addAttribute("total", repo.count());
        return "servicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("servico", new Servico());
        return "servicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("servico", repo.findById(id).orElseThrow());
        return "servicos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("servico") Servico servico, BindingResult erros,
                         RedirectAttributes redirect) {
        if (erros.hasErrors()) return "servicos/form";
        repo.save(servico);
        redirect.addFlashAttribute("sucesso", "Serviço " + servico.getNome() + " salvo.");
        return "redirect:/servicos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            repo.deleteById(id);
            repo.flush();
            redirect.addFlashAttribute("sucesso", "Serviço excluído.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: o serviço está em agendamentos. Desative-o em vez disso.");
        }
        return "redirect:/servicos";
    }
}
