package com.tiendalego.controller;

import com.tiendalego.model.Pedido;
import com.tiendalego.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Crear y consultar pedidos de compra")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping("/carrito")
    @Operation(summary = "Crear pedido desde el carrito completo")
    public ResponseEntity<?> crearDesdeCarrito(@RequestBody Map<String, String> body) {
        try {
            Pedido pedido = service.crearDesdeCarrito(
                    body.get("carritoId"),
                    body.get("usuarioId"),
                    body.get("usuarioNombre"),
                    body.get("metodoPago"),
                    body.get("direccionEnvio"),
                    body.get("ciudadEnvio"),
                    body.get("departamentoEnvio"),
                    body.get("telefonoEnvio"),
                    body.get("nombreDestinatario")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/directo")
    @Operation(summary = "Comprar un producto directamente sin carrito")
    public ResponseEntity<?> crearDirecto(@RequestBody Map<String, String> body) {
        try {
            Pedido pedido = service.crearDirecto(
                    body.get("productoId"),
                    Integer.parseInt(body.getOrDefault("cantidad", "1")),
                    body.get("usuarioId"),
                    body.get("usuarioNombre"),
                    body.get("metodoPago"),
                    body.get("direccionEnvio"),
                    body.get("ciudadEnvio"),
                    body.get("departamentoEnvio"),
                    body.get("telefonoEnvio"),
                    body.get("nombreDestinatario")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar pedidos de un usuario")
    public List<Pedido> listarPorUsuario(@PathVariable String usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos (admin)")
    public List<Pedido> listarTodos() {
        return service.listarTodos();
    }
}
