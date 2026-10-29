package com.tiendalego.repository;

import com.tiendalego.model.Imagen;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImagenRepository extends MongoRepository<Imagen, String> {
}
