package com.remediar.back_remediar.controller;

import com.remediar.back_remediar.model.UsuarioComum;
import com.remediar.back_remediar.model.UsuarioComumPF;
import com.remediar.back_remediar.model.UsuarioComumPJ;
import com.remediar.back_remediar.model.User;
import com.remediar.back_remediar.model.dto.notificacoes.NotificationRequestDto;
import com.remediar.back_remediar.model.dto.notificacoes.TipoCanal;
import com.remediar.back_remediar.model.dto.usuarios.UsuarioComumDTO;
import com.remediar.back_remediar.model.dto.usuarios.UsuarioComumPFDTO;
import com.remediar.back_remediar.producer.NotificationProducer;
import com.remediar.back_remediar.service.TwoFactorAuthenticationService;
import com.remediar.back_remediar.service.UsuarioComumService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios Comuns", description = "Operações relacionadas a usuários comuns")
public class UsuarioComumController {

    private final UsuarioComumService usuarioComumService;
    private final NotificationProducer notificationProducer;
    private TwoFactorAuthenticationService twoFactorAuthenticationService;

    public UsuarioComumController(UsuarioComumService usuarioComumService, NotificationProducer notificationProducer,
            TwoFactorAuthenticationService twoFactorAuthenticationService) {
        this.usuarioComumService = usuarioComumService;
        this.notificationProducer = notificationProducer;
        this.twoFactorAuthenticationService = twoFactorAuthenticationService;
    }

    @GetMapping
    public ResponseEntity<List<Record>> listar() {
        List<Record> clientes = usuarioComumService.listar();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        UsuarioComum usuarioComum = usuarioComumService.findById(id);

        if (usuarioComum instanceof UsuarioComumPF) {
            return ResponseEntity.ok(usuarioComumService.pfToDto((UsuarioComumPF) usuarioComum));
        } else {
            return ResponseEntity.ok(usuarioComumService.pjToDto((UsuarioComumPJ) usuarioComum));
        }
    }

    @PostMapping("/pj")
    public ResponseEntity<UsuarioComumDTO> createUsuarioComumPJ(@Valid @RequestBody UsuarioComumDTO obj) {
        UsuarioComumDTO newUsuario = usuarioComumService.createPJ(obj);

        // String codigo =
        // twoFactorAuthenticationService.gerarCodigo(obj.user().getLogin());

        // notificationProducer.sendNotification(new NotificationRequestDto(
        // newUsuario.id(),
        // newUsuario.user().getLogin(),
        // newUsuario.user().getLogin(),
        // "Verificação de dois fatores",
        // "Olá, " + newUsuario.nome()
        // + "! Seja bem vindo(a) à Remediar!"
        // + "\nSeu código de verificação é: " + codigo
        // + "\nAtenção, este código é válido por 15 minutos."
        // + "\nCaso não tenha solicitado, desconsidere esta mensagem.",
        // TipoCanal.EMAIL
        // ));

        // notificationProducer.sendNotification(new NotificationRequestDto(
        // newUsuario.id(),
        // newUsuario.user().getLogin(),
        // newUsuario.user().getLogin(),
        // "Verificação de dois fatores",
        // "Olá, " + newUsuario.nome() + "! Bem vindo(a) à Remediar!\nSeu código de
        // verificação é: " +
        // twoFactorAuthenticationService.gerarCodigo(newUsuario.user().getLogin()),
        // TipoCanal.EMAIL
        // ));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newUsuario.id())
                .toUri();

        return ResponseEntity.created(uri).body(newUsuario);
    }

    @PutMapping("/pj/{id}")
    public ResponseEntity<UsuarioComumDTO> atualizarPJ(@PathVariable Long id, @Valid @RequestBody UsuarioComumDTO obj) {
        UsuarioComumDTO usuarioComumDTOAtualizado = usuarioComumService.atualizarPJ(id, obj);
        return ResponseEntity.ok(usuarioComumDTOAtualizado);
    }

    @PostMapping("/pf")
    public ResponseEntity<UsuarioComumPFDTO> createUsuarioComumPF(@Valid @RequestBody UsuarioComumPFDTO obj) {
        UsuarioComumPFDTO newUsuario = usuarioComumService.createPF(obj);

        // String codigo =
        // twoFactorAuthenticationService.gerarCodigo(obj.usuario().user().getLogin());

        // notificationProducer.sendNotification(new NotificationRequestDto(
        // newUsuario.usuario().id(),
        // newUsuario.usuario().user().getLogin(),
        // newUsuario.usuario().user().getLogin(),
        // "Verificação de dois fatores",
        // "Olá, " + newUsuario.usuario().nome()
        // + "! Seja bem vindo(a) à Remediar!"
        // + "\nSeu código de verificação é: " + codigo
        // + "\nAtenção, este código é válido por 15 minutos."
        // + "\nCaso não tenha solicitado, desconsidere esta mensagem.",
        // TipoCanal.EMAIL
        // ));

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newUsuario.usuario().id())
                .toUri();

        return ResponseEntity.created(uri).body(newUsuario);
    }

    @PutMapping("/pf/{id}")
    public ResponseEntity<UsuarioComumPFDTO> atualizarPF(@PathVariable Long id,
            @Valid @RequestBody UsuarioComumPFDTO obj) {
        UsuarioComumPFDTO usuarioComumPFDTOAtualizado = usuarioComumService.atualizarPF(id, obj);
        return ResponseEntity.ok(usuarioComumPFDTOAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioComumService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verificar")
    public ResponseEntity<String> verificarConta(@RequestParam String email, @RequestParam String codigo) {
        if (usuarioComumService.isVerificado(email)) {
            return ResponseEntity.badRequest().body("Conta já verificada.");
        } else if (twoFactorAuthenticationService.validarCodigo(email, codigo)) {
            usuarioComumService.ativarUsuario(email);
            return ResponseEntity.ok("Conta verificada com sucesso!");
        }

        return ResponseEntity.badRequest().body("Código inválido.");
    }

    @PostMapping("/is-verificado")
    public ResponseEntity<Boolean> isVerificado(@RequestParam String login) {
        return ResponseEntity.ok(usuarioComumService.isVerificado(login));
    }

    @PostMapping("/reenviar-codigo")
    public ResponseEntity<String> reenviarCodigo(@RequestParam String email) {
        if (usuarioComumService.isVerificado(email)) {
            return ResponseEntity.badRequest().body("Conta já verificada.");
        }

        String codigo = twoFactorAuthenticationService.gerarCodigo(email);
        UsuarioComum usuario = usuarioComumService.findByEmail(email);

        notificationProducer.sendNotification(new NotificationRequestDto(
                usuario.getId(),
                email,
                email,
                "Verificação de dois fatores",
                "Olá, " + usuario.getNome()
                        + "! Seja bem vindo(a) à Remediar!"
                        + "\nSeu código de verificação é: " + codigo
                        + "\nAtenção, este código é válido por 15 minutos."
                        + "\nCaso não tenha solicitado, desconsidere esta mensagem.",
                TipoCanal.EMAIL));

        return ResponseEntity.ok("Código reenviado com sucesso.");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        UsuarioComum usuarioComum = user.getUsuarioComum();

        if (usuarioComum instanceof UsuarioComumPF) {
            return ResponseEntity.ok(usuarioComumService.pfToDto((UsuarioComumPF) usuarioComum));
        } else if (usuarioComum instanceof UsuarioComumPJ) {
            return ResponseEntity.ok(usuarioComumService.pjToDto((UsuarioComumPJ) usuarioComum));
        }

        return ResponseEntity.notFound().build();
    }
}
