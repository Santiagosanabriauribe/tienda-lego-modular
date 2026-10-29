package com.tiendalego.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Imagenes", description = "Subida de imagenes para productos")
public class ImagenController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    @PostMapping
    @Operation(summary = "Subir una imagen",
               description = "Sube un archivo de imagen y devuelve la URL relativa para usarla en un producto",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Imagen subida exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Archivo vacio o invalido")
               })
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
