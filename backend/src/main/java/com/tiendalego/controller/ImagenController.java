package com.tiendalego.controller;

import com.tiendalego.model.Imagen;
import com.tiendalego.repository.ImagenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/imagenes")
@Tag(name = "Imagenes", description = "Subida y consulta de imagenes para productos")
public class ImagenController {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp"
    );

    private final ImagenRepository repo;

    public ImagenController(ImagenRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    @Operation(summary = "Subir una imagen",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Imagen subida exitosamente"),
                   @ApiResponse(responseCode = "400", description = "Archivo vacio o tipo no permitido")
               })
    public ResponseEntity<?> subir(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacio"));
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tipo de archivo no permitido. Usa PNG, JPG, GIF o WEBP"));
        }

        Imagen imagen = new Imagen(
                archivo.getOriginalFilename(),
                contentType,
                archivo.getBytes()
        );
        imagen = repo.save(imagen);

        String url = "/api/v1/imagenes/" + imagen.getId();
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una imagen por ID")
    public ResponseEntity<byte[]> obtener(@PathVariable String id) {
        return repo.findById(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getContentType()))
                        .body(img.getDatos()))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
