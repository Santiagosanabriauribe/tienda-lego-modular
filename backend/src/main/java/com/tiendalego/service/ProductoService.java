package com.tiendalego.service;

import com.tiendalego.model.Producto;
import com.tiendalego.repository.CategoriaRepository;
import com.tiendalego.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;

    public ProductoService(ProductoRepository productoRepo, CategoriaRepository categoriaRepo) {
        this.productoRepo = productoRepo;
        this.categoriaRepo = categoriaRepo;
    }

    public List<Producto> listarActivos() {
        return productoRepo.findByActivoTrue();
    }

    public List<Producto> listarTodos() {
        return productoRepo.findAll();
    }

    public List<Producto> listarPorCategoria(String categoriaId) {
        return productoRepo.findByCategoriaIdAndActivoTrue(categoriaId);
    }

    public Optional<Producto> buscarPorId(String id) {
        return productoRepo.findById(id);
    }

    public Producto crear(Producto producto) {
        if (!categoriaRepo.existsById(producto.getCategoriaId())) {
            throw new IllegalArgumentException("La categoria con id " + producto.getCategoriaId() + " no existe");
        }
        producto.setId(null);
        producto.setActivo(true);
        return productoRepo.save(producto);
    }

    public Optional<Producto> actualizar(String id, Producto datos) {
        return productoRepo.findById(id).map(existente -> {
            if (datos.getCategoriaId() != null && !categoriaRepo.existsById(datos.getCategoriaId())) {
                throw new IllegalArgumentException("La categoria con id " + datos.getCategoriaId() + " no existe");
            }
            existente.setNombre(datos.getNombre());
            existente.setNumeroSet(datos.getNumeroSet());
            existente.setDescripcion(datos.getDescripcion());
            existente.setPrecio(datos.getPrecio());
            existente.setStock(datos.getStock());
            existente.setImagenUrl(datos.getImagenUrl());
            existente.setActivo(datos.isActivo());
            existente.setCategoriaId(datos.getCategoriaId());
            return productoRepo.save(existente);
        });
    }

    public boolean eliminar(String id) {
        return productoRepo.findById(id).map(prod -> {
            productoRepo.delete(prod);
            return true;
        }).orElse(false);
    }
}
