package com.tiendalego.controller;

import com.tiendalego.model.Usuario;
import com.tiendalego.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro e inicio de sesion")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un nuevo usuario")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String email = body.get("email");
        String password = body.get("password");
        String rol = body.getOrDefault("rol", "USUARIO");

        if (nombre == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nombre, email y password son obligatorios"));
        }

        try {
            Usuario usuario = service.registrar(nombre, email, password, rol);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSinPassword(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email y password son obligatorios"));
        }

        return service.login(email, password)
                .map(u -> ResponseEntity.ok(usuarioSinPassword(u)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Email o contraseña incorrectos")));
    }

    private Map<String, String> usuarioSinPassword(Usuario u) {
        return Map.of(
                "id", u.getId(),
                "nombre", u.getNombre(),
                "email", u.getEmail(),
                "rol", u.getRol()
        );
    }
}
