package br.com.mercadoexpress.web;

import br.com.mercadoexpress.domain.usuario.Perfil;
import br.com.mercadoexpress.exception.IdNaoEncontradoException;
import br.com.mercadoexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String listar(Model model, Authentication auth) {
        model.addAttribute("usuarios", usuarioRepository.findAll(Sort.by("username")));
        model.addAttribute("usuarioLogado", auth.getName());
        return "admin/usuarios";
    }

    @PostMapping("/{id}/perfil")
    public String alterarPerfil(@PathVariable Long id, @RequestParam Perfil perfil,
                                Authentication auth, RedirectAttributes redirect) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException(id));

        if (usuario.getUsername().equals(auth.getName())) {
            redirect.addFlashAttribute("erro", "Você não pode alterar o seu próprio perfil.");
            return "redirect:/admin/usuarios";
        }

        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
        redirect.addFlashAttribute("sucesso",
                usuario.getUsername() + " agora é " + (perfil == Perfil.ADMIN ? "administrador" : "usuário") + ".");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, Authentication auth, RedirectAttributes redirect) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException(id));

        if (usuario.getUsername().equals(auth.getName())) {
            redirect.addFlashAttribute("erro", "Você não pode excluir a sua própria conta.");
            return "redirect:/admin/usuarios";
        }

        usuarioRepository.delete(usuario);
        redirect.addFlashAttribute("sucesso", "Usuário " + usuario.getUsername() + " excluído.");
        return "redirect:/admin/usuarios";
    }
}