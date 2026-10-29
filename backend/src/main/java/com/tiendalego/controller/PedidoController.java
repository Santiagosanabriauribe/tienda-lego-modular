package com.tiendalego.controller;

import com.tiendalego.model.Pedido;
import com.tiendalego.model.Usuario;
import com.tiendalego.service.PedidoService;
import com.tiendalego.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Crear y consultar pedidos de compra")
public class PedidoController {

    private final PedidoService service;
    private final UsuarioService usuarioService;

    public PedidoController(PedidoService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    private boolean esAdmin(Authentication auth) {
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private Usuario obtenerUsuario(Authentication auth) {
        return usuarioService.buscarPorId(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/carrito")
    @Operation(summary = "Crear pedido desde el carrito completo")
    public ResponseEntity<?> crearDesdeCarrito(@RequestBody Map<String, String> body,
                                               Authentication auth) {
        try {
            Usuario u = obtenerUsuario(auth);
            Pedido pedido = service.crearDesdeCarrito(
                    body.get("carritoId"), u.getId(), u.getNombre(),
                    body.get("metodoPago"),
                    body.get("direccionEnvio"), body.get("ciudadEnvio"),
                    body.get("departamentoEnvio"), body.get("telefonoEnvio"),
                    body.get("nombreDestinatario")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/directo")
    @Operation(summary = "Comprar un producto directamente sin carrito")
    public ResponseEntity<?> crearDirecto(@RequestBody Map<String, String> body,
                                           Authentication auth) {
        try {
            Usuario u = obtenerUsuario(auth);
            Pedido pedido = service.crearDirecto(
                    body.get("productoId"),
                    Integer.parseInt(body.getOrDefault("cantidad", "1")),
                    u.getId(), u.getNombre(),
                    body.get("metodoPago"),
                    body.get("direccionEnvio"), body.get("ciudadEnvio"),
                    body.get("departamentoEnvio"), body.get("telefonoEnvio"),
                    body.get("nombreDestinatario")
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un pedido por ID (solo el dueño o admin)")
    public Pedido uno(@PathVariable String id, Authentication auth) {
        return service.buscarPorId(id)
                .filter(p -> esAdmin(auth) || p.getUsuarioId().equals(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No encontrado"));
    }

    @GetMapping("/mis-pedidos")
    @Operation(summary = "Listar pedidos del usuario autenticado")
    public List<Pedido> misPedidos(Authentication auth) {
        return service.listarPorUsuario(auth.getName());
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos (solo admin)")
    public List<Pedido> listarTodos(Authentication auth) {
        return esAdmin(auth) ? service.listarTodos() : service.listarPorUsuario(auth.getName());
    }
}
