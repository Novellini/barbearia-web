package br.com.barbearia.web.controller;

import br.com.barbearia.web.model.Barbeiro;
import br.com.barbearia.web.repository.BarbeiroRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/barbeiros")
public class BarbeiroController {

    private final BarbeiroRepository repo;

    public BarbeiroController(BarbeiroRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String busca, Model model) {
        boolean filtrando = busca != null && !busca.isBlank();
        model.addAttribute("barbeiros", filtrando ? repo.buscar(busca.trim()) : repo.findAllByOrderByNome());
        model.addAttribute("busca", busca);
        model.addAttribute("total", repo.count());
        return "barbeiros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("barbeiro", new Barbeiro());
        return "barbeiros/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("barbeiro", repo.findById(id).orElseThrow());
        return "barbeiros/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("barbeiro") Barbeiro barbeiro, BindingResult erros,
                         RedirectAttributes redirect) {
        if (erros.hasErrors()) return "barbeiros/form";
        repo.save(barbeiro);
        redirect.addFlashAttribute("sucesso", "Barbeiro " + barbeiro.getNome() + " salvo.");
        return "redirect:/barbeiros";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            repo.deleteById(id);
            repo.flush();
            redirect.addFlashAttribute("sucesso", "Barbeiro excluído.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: o barbeiro tem agendamentos. Desative-o em vez disso.");
        }
        return "redirect:/barbeiros";
    }
}
