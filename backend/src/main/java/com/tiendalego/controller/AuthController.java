package com.tiendalego.controller;

import com.tiendalego.config.JwtService;
import com.tiendalego.config.LimitadorLogin;
import com.tiendalego.model.Usuario;
import com.tiendalego.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro, login y logout con JWT")
public class AuthController {

    private final UsuarioService service;
    private final JwtService jwt;
    private final LimitadorLogin limitador;

    public AuthController(UsuarioService service, JwtService jwt, LimitadorLogin limitador) {
        this.service = service;
        this.jwt = jwt;
        this.limitador = limitador;
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
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("ok", true, "rol", usuario.getRol()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion — devuelve token en cookie HttpOnly")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest req) {
        if (!limitador.permitido(req.getRemoteAddr())) {
            return ResponseEntity.status(429)
                    .body(Map.of("error", "Demasiados intentos, espere 15 minutos"));
        }

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email y password son obligatorios"));
        }

        return service.login(email, password)
                .map(u -> {
                    String token = jwt.crear(u.getId(), u.getRol());
                    ResponseCookie cookie = ResponseCookie.from("token", token)
                            .httpOnly(true).secure(true).sameSite("Strict")
                            .path("/").maxAge(Duration.ofMinutes(15)).build();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(Map.of("ok", true, "rol", u.getRol()));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario o contraseña incorrectos")));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesion — borra la cookie del token")
    public ResponseEntity<?> logout() {
        ResponseCookie borrar = ResponseCookie.from("token", "")
                .httpOnly(true).secure(true).sameSite("Strict")
                .path("/").maxAge(0).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, borrar.toString())
                .body(Map.of("ok", true));
    }
}
