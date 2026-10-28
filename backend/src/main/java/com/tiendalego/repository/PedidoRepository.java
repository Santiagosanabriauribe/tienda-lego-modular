package com.tiendalego.repository;

import com.tiendalego.model.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findByUsuarioIdOrderByFechaDesc(String usuarioId);
}
