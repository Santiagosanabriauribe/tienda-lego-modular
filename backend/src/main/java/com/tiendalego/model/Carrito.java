package com.tiendalego.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "carritos")
public class Carrito {

    @Id
    private String id;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String estado = "ACTIVO";

    private BigDecimal total = BigDecimal.ZERO;

    private List<ItemCarrito> items = new ArrayList<>();

    public Carrito() {}

    public void recalcularTotal() {
        this.total = items.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<ItemCarrito> getItems() { return items; }
    public void setItems(List<ItemCarrito> items) { this.items = items; }
}
