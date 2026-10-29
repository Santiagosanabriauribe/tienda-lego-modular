package com.tiendalego.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "imagenes")
public class Imagen {

    @Id
    private String id;
    private String nombre;
    private String contentType;
    private byte[] datos;
    private LocalDateTime fecha = LocalDateTime.now();

    public Imagen() {}

    public Imagen(String nombre, String contentType, byte[] datos) {
        this.nombre = nombre;
        this.contentType = contentType;
        this.datos = datos;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
