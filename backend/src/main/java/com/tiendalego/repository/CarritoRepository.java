package com.tiendalego.repository;

import com.tiendalego.model.Carrito;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarritoRepository extends MongoRepository<Carrito, String> {
}
