package com.tiendalego.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ErroresGlobales {

    private static final Logger log = LoggerFactory.getLogger(ErroresGlobales.class);

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> estado(ResponseStatusException e) {
        String msg = e.getReason() == null ? "Error" : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalido(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Datos invalidos"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> otro(Exception e) {
        log.error("Error no controlado", e);
        return ResponseEntity.status(500).body(Map.of("error", "Error interno"));
    }
}
