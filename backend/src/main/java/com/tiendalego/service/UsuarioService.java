package com.tiendalego.service;

import com.tiendalego.model.Usuario;
import com.tiendalego.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    public Usuario registrar(String nombre, String email, String password, String rol) {
        if (repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        Usuario usuario = new Usuario(nombre, email, hashPassword(password), rol);
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
