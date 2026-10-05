package com.tiendalego.service;

import com.tiendalego.model.Carrito;
import com.tiendalego.model.ItemCarrito;
import com.tiendalego.model.Producto;
import com.tiendalego.repository.CarritoRepository;
import com.tiendalego.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepo;
    private final ProductoRepository productoRepo;

    public CarritoService(CarritoRepository carritoRepo, ProductoRepository productoRepo) {
        this.carritoRepo = carritoRepo;
        this.productoRepo = productoRepo;
    }

    public Carrito crearCarrito() {
        return carritoRepo.save(new Carrito());
    }

    public Optional<Carrito> buscarPorId(String id) {
        return carritoRepo.findById(id);
    }

    public Optional<Carrito> agregarItem(String carritoId, String productoId, int cantidad) {
        return carritoRepo.findById(carritoId).map(carrito -> {
            Producto producto = productoRepo.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

            if (cantidad > producto.getStock()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente. Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
            }

            Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                    .filter(i -> i.getProductoId().equals(productoId))
                    .findFirst();

            if (itemExistente.isPresent()) {
                ItemCarrito item = itemExistente.get();
                int nuevaCantidad = item.getCantidad() + cantidad;
                if (nuevaCantidad > producto.getStock()) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente. Disponible: " + producto.getStock()
                                    + ", en carrito: " + item.getCantidad() + ", solicitado: " + cantidad);
                }
                item.setCantidad(nuevaCantidad);
                item.recalcularSubtotal();
            } else {
                carrito.getItems().add(new ItemCarrito(
                        producto.getId(), producto.getNombre(), cantidad, producto.getPrecio()));
            }

            carrito.recalcularTotal();
            return carritoRepo.save(carrito);
        });
    }

    public Optional<Carrito> actualizarCantidad(String carritoId, String productoId, int nuevaCantidad) {
        return carritoRepo.findById(carritoId).map(carrito -> {
            ItemCarrito item = carrito.getItems().stream()
                    .filter(i -> i.getProductoId().equals(productoId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Producto no esta en el carrito"));

            Producto producto = productoRepo.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            if (nuevaCantidad > producto.getStock()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente. Disponible: " + producto.getStock());
            }

            if (nuevaCantidad <= 0) {
                carrito.getItems().remove(item);
            } else {
                item.setCantidad(nuevaCantidad);
                item.recalcularSubtotal();
            }

            carrito.recalcularTotal();
            return carritoRepo.save(carrito);
        });
    }

    public Optional<Carrito> eliminarItem(String carritoId, String productoId) {
        return carritoRepo.findById(carritoId).map(carrito -> {
            carrito.getItems().removeIf(i -> i.getProductoId().equals(productoId));
            carrito.recalcularTotal();
            return carritoRepo.save(carrito);
        });
    }
}
