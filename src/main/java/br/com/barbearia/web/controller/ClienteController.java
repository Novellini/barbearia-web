package br.com.barbearia.web.controller;

import br.com.barbearia.web.model.Cliente;
import br.com.barbearia.web.repository.ClienteRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Tela 5 do wireframe: clientes
@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository repo;

    public ClienteController(ClienteRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String busca, Model model) {
        boolean filtrando = busca != null && !busca.isBlank();
        model.addAttribute("clientes", filtrando ? repo.buscar(busca.trim()) : repo.findAllByOrderByNome());
        model.addAttribute("busca", busca);
        model.addAttribute("total", repo.count());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("cliente", repo.findById(id).orElseThrow());
        return "clientes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult erros,
                         RedirectAttributes redirect) {
        if (erros.hasErrors()) return "clientes/form";
        try {
            repo.saveAndFlush(cliente);
        } catch (DataIntegrityViolationException ex) {
            erros.rejectValue("telefone", "duplicado", "Já existe um cliente com esse telefone");
            return "clientes/form";
        }
        redirect.addFlashAttribute("sucesso", "Cliente " + cliente.getNome() + " salvo.");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            repo.deleteById(id);
            repo.flush();
            redirect.addFlashAttribute("sucesso", "Cliente excluído.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: o cliente tem agendamentos.");
        }
        return "redirect:/clientes";
    }
}
