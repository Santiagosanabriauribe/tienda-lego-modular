package com.tiendalego.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/imagenes")
public class ImagenController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    @PostMapping
    public ResponseEntity<?> subir(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacio"));
        }

        Files.createDirectories(UPLOAD_DIR);

        String extension = "";
        String original = archivo.getOriginalFilename();
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        String nombre = UUID.randomUUID() + extension;

        Path destino = UPLOAD_DIR.resolve(nombre);
        archivo.transferTo(destino.toFile());

        String url = "/uploads/" + nombre;
        return ResponseEntity.ok(Map.of("url", url));
    }
}
