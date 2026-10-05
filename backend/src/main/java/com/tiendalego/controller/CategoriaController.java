package com.tiendalego.controller;

import com.tiendalego.model.Categoria;
import com.tiendalego.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorias", description = "Operaciones CRUD para categorias de productos LEGO")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar categorias activas",
               description = "Devuelve todas las categorias que estan activas en el sistema")
    public List<Categoria> listar() {
        return service.listarActivas();
    }

    @GetMapping("/todas")
    @Operation(summary = "Listar todas las categorias",
               description = "Devuelve todas las categorias incluyendo las inactivas (uso administrativo)")
    public List<Categoria> listarTodas() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
                   @ApiResponse(responseCode = "404", description = "Categoria no encontrada")
               })
    public ResponseEntity<Categoria> buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear una nueva categoria",
               description = "Crea una categoria con nombre y descripcion")
    @ApiResponse(responseCode = "201", description = "Categoria creada exitosamente")
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria categoria) {
        Categoria creada = service.crear(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoria existente",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Categoria actualizada"),
                   @ApiResponse(responseCode = "404", description = "Categoria no encontrada")
               })
    public ResponseEntity<Categoria> actualizar(@PathVariable String id,
                                                 @Valid @RequestBody Categoria categoria) {
        return service.actualizar(id, categoria)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoria",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Categoria eliminada"),
                   @ApiResponse(responseCode = "404", description = "Categoria no encontrada")
               })
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
