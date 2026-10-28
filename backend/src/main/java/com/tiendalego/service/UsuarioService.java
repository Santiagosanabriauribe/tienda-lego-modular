package com.tiendalego.service;

import com.tiendalego.model.Usuario;
import com.tiendalego.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Bean
    CommandLineRunner crearAdminInicial() {
        return args -> {
            if (!repo.existsByEmail("admin@tiendalego.com")) {
                Usuario admin = new Usuario("Administrador", "admin@tiendalego.com",
                        hashPassword("admin1234"), "ADMIN");
                repo.save(admin);
            }
        };
    }

    public Usuario registrar(String nombre, String email, String password, String rol) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener minimo 8 caracteres");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El formato del correo electronico no es valido");
        }
        if (repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        if ("ADMIN".equalsIgnoreCase(rol)) {
            throw new IllegalArgumentException("No es posible registrar una cuenta de administrador");
        }
        Usuario usuario = new Usuario(nombre, email, hashPassword(password), "USUARIO");
        return repo.save(usuario);
    }

    public Optional<Usuario> login(String email, String password) {
        return repo.findByEmail(email)
                .filter(u -> u.getPassword().equals(hashPassword(password)));
    }

    public Optional<Usuario> buscarPorId(String id) {
        return repo.findById(id);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
