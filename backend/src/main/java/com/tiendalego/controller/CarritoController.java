package com.tiendalego.controller;

import com.tiendalego.model.Carrito;
import com.tiendalego.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carritos")
@Tag(name = "Carrito de Compras", description = "Operaciones del carrito: crear, agregar, modificar y eliminar items")
public class CarritoController {

    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo carrito",
               description = "Crea un carrito vacio para que el cliente agregue productos")
    @ApiResponse(responseCode = "201", description = "Carrito creado")
    public ResponseEntity<Carrito> crearCarrito() {
        Carrito carrito = service.crearCarrito();
        return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un carrito",
               description = "Devuelve el carrito con sus items y el total calculado",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
                   @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
               })
    public ResponseEntity<Carrito> buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{carritoId}/items")
    @Operation(summary = "Agregar producto al carrito",
               description = "Agrega un producto con la cantidad indicada. Valida stock disponible.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Producto agregado al carrito"),
                   @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
                   @ApiResponse(responseCode = "400", description = "Stock insuficiente o producto no encontrado")
               })
    public ResponseEntity<Carrito> agregarItem(
            @PathVariable String carritoId,
            @Parameter(description = "ID del producto a agregar", required = true)
            @RequestParam String productoId,
            @Parameter(description = "Cantidad de unidades", required = true)
            @RequestParam int cantidad) {
        return service.agregarItem(carritoId, productoId, cantidad)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{carritoId}/items/{productoId}")
    @Operation(summary = "Actualizar cantidad de un producto en el carrito",
               description = "Cambia la cantidad. Si es 0 o menor, elimina el item.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Cantidad actualizada"),
                   @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
                   @ApiResponse(responseCode = "400", description = "Stock insuficiente")
               })
    public ResponseEntity<Carrito> actualizarCantidad(
            @PathVariable String carritoId,
            @PathVariable String productoId,
            @Parameter(description = "Nueva cantidad", required = true)
            @RequestParam int cantidad) {
        return service.actualizarCantidad(carritoId, productoId, cantidad)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{carritoId}/items/{productoId}")
    @Operation(summary = "Eliminar un producto del carrito",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Producto eliminado del carrito"),
                   @ApiResponse(responseCode = "404", description = "Carrito no encontrado")
               })
    public ResponseEntity<Carrito> eliminarItem(
            @PathVariable String carritoId,
            @PathVariable String productoId) {
        return service.eliminarItem(carritoId, productoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
