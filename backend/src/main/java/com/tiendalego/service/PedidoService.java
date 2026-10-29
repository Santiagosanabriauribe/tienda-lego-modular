package com.tiendalego.service;

import com.tiendalego.model.*;
import com.tiendalego.repository.CarritoRepository;
import com.tiendalego.repository.PedidoRepository;
import com.tiendalego.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final CarritoRepository carritoRepo;
    private final ProductoRepository productoRepo;

    public PedidoService(PedidoRepository pedidoRepo, CarritoRepository carritoRepo,
                         ProductoRepository productoRepo) {
        this.pedidoRepo = pedidoRepo;
        this.carritoRepo = carritoRepo;
        this.productoRepo = productoRepo;
    }

    public Pedido crearDesdeCarrito(String carritoId, String usuarioId, String usuarioNombre,
                                    String metodoPago, String direccion, String ciudad,
                                    String departamento, String telefono, String destinatario) {
        Carrito carrito = carritoRepo.findById(carritoId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito esta vacio");
        }

        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getProductoNombre()));
            if (item.getCantidad() > producto.getStock()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepo.save(producto);
        }

        if (direccion == null || direccion.isBlank() || ciudad == null || ciudad.isBlank()
                || departamento == null || departamento.isBlank() || telefono == null || telefono.isBlank()
                || destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("Todos los datos de envio son obligatorios");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setUsuarioNombre(usuarioNombre);
        pedido.setItems(List.copyOf(carrito.getItems()));
        pedido.setTotal(carrito.getTotal());
        pedido.setMetodoPago(metodoPago);
        pedido.setDireccionEnvio(direccion);
        pedido.setCiudadEnvio(ciudad);
        pedido.setDepartamentoEnvio(departamento);
        pedido.setTelefonoEnvio(telefono);
        pedido.setNombreDestinatario(destinatario);

        carrito.getItems().clear();
        carrito.setTotal(BigDecimal.ZERO);
        carrito.setEstado("COMPRADO");
        carritoRepo.save(carrito);

        return pedidoRepo.save(pedido);
    }

    public Pedido crearDirecto(String productoId, int cantidad, String usuarioId,
                                String usuarioNombre, String metodoPago, String direccion,
                                String ciudad, String departamento, String telefono,
                                String destinatario) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (cantidad > producto.getStock()) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        producto.setStock(producto.getStock() - cantidad);
        productoRepo.save(producto);

        ItemCarrito item = new ItemCarrito(producto.getId(), producto.getNombre(),
                cantidad, producto.getPrecio());

        if (direccion == null || direccion.isBlank() || ciudad == null || ciudad.isBlank()
                || departamento == null || departamento.isBlank() || telefono == null || telefono.isBlank()
                || destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("Todos los datos de envio son obligatorios");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setUsuarioNombre(usuarioNombre);
        pedido.setItems(List.of(item));
        pedido.setTotal(item.getSubtotal());
        pedido.setMetodoPago(metodoPago);
        pedido.setDireccionEnvio(direccion);
        pedido.setCiudadEnvio(ciudad);
        pedido.setDepartamentoEnvio(departamento);
        pedido.setTelefonoEnvio(telefono);
        pedido.setNombreDestinatario(destinatario);

        return pedidoRepo.save(pedido);
    }

    public java.util.Optional<Pedido> buscarPorId(String id) {
        return pedidoRepo.findById(id);
    }

    public List<Pedido> listarPorUsuario(String usuarioId) {
        return pedidoRepo.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    public List<Pedido> listarTodos() {
        return pedidoRepo.findAll();
    }
}
