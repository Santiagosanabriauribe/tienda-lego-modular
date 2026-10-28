package com.tiendalego.service;

import com.tiendalego.model.Producto;
import com.tiendalego.repository.CategoriaRepository;
import com.tiendalego.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepo;

    @Mock
    private CategoriaRepository categoriaRepo;

    @InjectMocks
    private ProductoService service;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId("prod-1");
        producto.setNombre("Halcon Milenario");
        producto.setNumeroSet("75192");
        producto.setPrecio(new BigDecimal("849.99"));
        producto.setStock(4);
        producto.setActivo(true);
        producto.setCategoriaId("cat-1");
    }

    @Test
    void listarActivos_devuelveProductosActivos() {
        when(productoRepo.findByActivoTrue()).thenReturn(List.of(producto));

        List<Producto> resultado = service.listarActivos();

        assertEquals(1, resultado.size());
        assertEquals("Halcon Milenario", resultado.get(0).getNombre());
        verify(productoRepo).findByActivoTrue();
    }

    @Test
    void listarPorCategoria_filtraCorrectamente() {
        when(productoRepo.findByCategoriaIdAndActivoTrue("cat-1")).thenReturn(List.of(producto));

        List<Producto> resultado = service.listarPorCategoria("cat-1");

        assertEquals(1, resultado.size());
        assertEquals("cat-1", resultado.get(0).getCategoriaId());
    }

    @Test
    void buscarPorId_existente_devuelveProducto() {
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));

        Optional<Producto> resultado = service.buscarPorId("prod-1");

        assertTrue(resultado.isPresent());
        assertEquals("Halcon Milenario", resultado.get().getNombre());
    }

    @Test
    void buscarPorId_inexistente_devuelveVacio() {
        when(productoRepo.findById("no-existe")).thenReturn(Optional.empty());

        Optional<Producto> resultado = service.buscarPorId("no-existe");

        assertFalse(resultado.isPresent());
    }

    @Test
    void crear_conCategoriaValida_guardaProducto() {
        when(categoriaRepo.existsById("cat-1")).thenReturn(true);
        when(productoRepo.save(any(Producto.class))).thenReturn(producto);

        Producto resultado = service.crear(producto);

        assertNotNull(resultado);
        assertEquals("Halcon Milenario", resultado.getNombre());
        verify(productoRepo).save(any(Producto.class));
    }

    @Test
    void crear_conCategoriaInvalida_lanzaExcepcion() {
        when(categoriaRepo.existsById("cat-inexistente")).thenReturn(false);

        Producto invalido = new Producto();
        invalido.setCategoriaId("cat-inexistente");
        invalido.setNombre("Test");
        invalido.setPrecio(BigDecimal.TEN);
        invalido.setStock(1);

        assertThrows(IllegalArgumentException.class, () -> service.crear(invalido));
        verify(productoRepo, never()).save(any());
    }

    @Test
    void actualizar_existente_modificaCampos() {
        Producto datos = new Producto();
        datos.setNombre("Halcon Milenario Edicion Especial");
        datos.setPrecio(new BigDecimal("999.99"));
        datos.setStock(2);
        datos.setActivo(true);
        datos.setCategoriaId("cat-1");

        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));
        when(categoriaRepo.existsById("cat-1")).thenReturn(true);
        when(productoRepo.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Producto> resultado = service.actualizar("prod-1", datos);

        assertTrue(resultado.isPresent());
        assertEquals("Halcon Milenario Edicion Especial", resultado.get().getNombre());
        assertEquals(new BigDecimal("999.99"), resultado.get().getPrecio());
    }

    @Test
    void eliminar_existente_devuelveTrue() {
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));

        assertTrue(service.eliminar("prod-1"));
        verify(productoRepo).delete(producto);
    }

    @Test
    void eliminar_inexistente_devuelveFalse() {
        when(productoRepo.findById("no-existe")).thenReturn(Optional.empty());

        assertFalse(service.eliminar("no-existe"));
        verify(productoRepo, never()).delete(any());
    }
}
