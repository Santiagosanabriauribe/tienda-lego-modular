package com.tiendalego.service;

import com.tiendalego.model.Usuario;
import com.tiendalego.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Bean
    CommandLineRunner crearAdminInicial() {
        return args -> {
            repo.findByEmail("admin@tiendalego.com").ifPresentOrElse(
                    admin -> {
                        admin.setPassword(encoder.encode("admin1234"));
                        repo.save(admin);
                    },
                    () -> repo.save(new Usuario("Administrador", "admin@tiendalego.com",
                            encoder.encode("admin1234"), "ADMIN"))
            );
        };
    }

    public Usuario registrar(String nombre, String email, String password, String rol) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener minimo 8 caracteres");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El formato del correo electronico no es valido");
        }
        String emailNorm = email.trim().toLowerCase();
        if (repo.existsByEmail(emailNorm)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        if ("ADMIN".equalsIgnoreCase(rol)) {
            throw new IllegalArgumentException("No es posible registrar una cuenta de administrador");
        }
        Usuario usuario = new Usuario(nombre, emailNorm, encoder.encode(password), "USUARIO");
        return repo.save(usuario);
    }

    public Optional<Usuario> login(String email, String password) {
        String emailNorm = String.valueOf(email).trim().toLowerCase();
        return repo.findByEmail(emailNorm)
                .filter(u -> encoder.matches(String.valueOf(password), u.getPassword()));
    }

    public Optional<Usuario> buscarPorId(String id) {
        return repo.findById(id);
    }
}
