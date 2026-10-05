package com.tiendalego.repository;

import com.tiendalego.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductoRepository extends MongoRepository<Producto, String> {

    List<Producto> findByCategoriaIdAndActivoTrue(String categoriaId);

    List<Producto> findByActivoTrue();
}
