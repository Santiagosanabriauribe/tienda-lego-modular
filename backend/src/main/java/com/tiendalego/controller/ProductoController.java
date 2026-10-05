package com.tiendalego.controller;

import com.tiendalego.model.Producto;
import com.tiendalego.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Operaciones CRUD para productos/sets LEGO")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar productos activos",
               description = "Devuelve todos los productos activos del catalogo. "
                           + "Opcionalmente filtra por categoria.")
    public List<Producto> listar(
            @Parameter(description = "ID de la categoria para filtrar")
            @RequestParam(required = false) String categoriaId) {
        if (categoriaId != null) {
            return service.listarPorCategoria(categoriaId);
        }
        return service.listarActivos();
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todos los productos",
               description = "Incluye productos inactivos (uso administrativo)")
    public List<Producto> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar producto por ID",
               description = "Devuelve la informacion completa de un producto",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Producto encontrado"),
                   @ApiResponse(responseCode = "404", description = "Producto no encontrado")
               })
    public ResponseEntity<Producto> buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto",
               description = "Registra un producto LEGO asociado a una categoria existente")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        Producto creado = service.crear(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto existente",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Producto actualizado"),
                   @ApiResponse(responseCode = "404", description = "Producto no encontrado")
               })
    public ResponseEntity<Producto> actualizar(@PathVariable String id,
                                                @Valid @RequestBody Producto producto) {
        return service.actualizar(id, producto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto",
               responses = {
                   @ApiResponse(responseCode = "204", description = "Producto eliminado"),
                   @ApiResponse(responseCode = "404", description = "Producto no encontrado")
               })
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
