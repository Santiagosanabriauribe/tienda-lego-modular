package com.tiendalego.service;

import com.tiendalego.model.Carrito;
import com.tiendalego.model.ItemCarrito;
import com.tiendalego.model.Producto;
import com.tiendalego.repository.CarritoRepository;
import com.tiendalego.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepo;

    @Mock
    private ProductoRepository productoRepo;

    @InjectMocks
    private CarritoService service;

    private Carrito carrito;
    private Producto producto;

    @BeforeEach
    void setUp() {
        carrito = new Carrito();
        carrito.setId("carrito-1");

        producto = new Producto();
        producto.setId("prod-1");
        producto.setNombre("Halcon Milenario");
        producto.setPrecio(new BigDecimal("849.99"));
        producto.setStock(5);
    }

    @Test
    void crearCarrito_devuelveCarritoNuevo() {
        Carrito nuevo = new Carrito();
        nuevo.setId("carrito-nuevo");
        when(carritoRepo.save(any(Carrito.class))).thenReturn(nuevo);

        Carrito resultado = service.crearCarrito();

        assertNotNull(resultado);
        assertEquals("carrito-nuevo", resultado.getId());
        verify(carritoRepo).save(any(Carrito.class));
    }

    @Test
    void buscarPorId_existente_devuelveCarrito() {
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));

        Optional<Carrito> resultado = service.buscarPorId("carrito-1");

        assertTrue(resultado.isPresent());
        assertEquals("carrito-1", resultado.get().getId());
    }

    @Test
    void buscarPorId_inexistente_devuelveVacio() {
        when(carritoRepo.findById("no-existe")).thenReturn(Optional.empty());

        Optional<Carrito> resultado = service.buscarPorId("no-existe");

        assertFalse(resultado.isPresent());
    }

    @Test
    void agregarItem_productoNuevo_agregaAlCarrito() {
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Carrito> resultado = service.agregarItem("carrito-1", "prod-1", 2);

        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getItems().size());
        assertEquals(2, resultado.get().getItems().get(0).getCantidad());
        assertEquals("Halcon Milenario", resultado.get().getItems().get(0).getProductoNombre());
    }

    @Test
    void agregarItem_productoExistente_incrementaCantidad() {
        carrito.getItems().add(new ItemCarrito("prod-1", "Halcon Milenario", 1, new BigDecimal("849.99")));
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Carrito> resultado = service.agregarItem("carrito-1", "prod-1", 2);

        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getItems().size());
        assertEquals(3, resultado.get().getItems().get(0).getCantidad());
    }

    @Test
    void agregarItem_stockInsuficiente_lanzaExcepcion() {
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class,
                () -> service.agregarItem("carrito-1", "prod-1", 10));
        verify(carritoRepo, never()).save(any());
    }

    @Test
    void agregarItem_productoNoExiste_lanzaExcepcion() {
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(productoRepo.findById("prod-x")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.agregarItem("carrito-1", "prod-x", 1));
    }

    @Test
    void agregarItem_carritoNoExiste_devuelveVacio() {
        when(carritoRepo.findById("no-existe")).thenReturn(Optional.empty());

        Optional<Carrito> resultado = service.agregarItem("no-existe", "prod-1", 1);

        assertFalse(resultado.isPresent());
    }

    @Test
    void actualizarCantidad_cantidadValida_actualizaItem() {
        carrito.getItems().add(new ItemCarrito("prod-1", "Halcon Milenario", 2, new BigDecimal("849.99")));
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Carrito> resultado = service.actualizarCantidad("carrito-1", "prod-1", 4);

        assertTrue(resultado.isPresent());
        assertEquals(4, resultado.get().getItems().get(0).getCantidad());
    }

    @Test
    void actualizarCantidad_cero_eliminaItem() {
        carrito.getItems().add(new ItemCarrito("prod-1", "Halcon Milenario", 2, new BigDecimal("849.99")));
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Carrito> resultado = service.actualizarCantidad("carrito-1", "prod-1", 0);

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getItems().isEmpty());
    }

    @Test
    void actualizarCantidad_stockInsuficiente_lanzaExcepcion() {
        carrito.getItems().add(new ItemCarrito("prod-1", "Halcon Milenario", 2, new BigDecimal("849.99")));
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(productoRepo.findById("prod-1")).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarCantidad("carrito-1", "prod-1", 99));
    }

    @Test
    void actualizarCantidad_productoNoEnCarrito_lanzaExcepcion() {
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarCantidad("carrito-1", "prod-x", 2));
    }

    @Test
    void eliminarItem_existente_eliminaDelCarrito() {
        carrito.getItems().add(new ItemCarrito("prod-1", "Halcon Milenario", 2, new BigDecimal("849.99")));
        when(carritoRepo.findById("carrito-1")).thenReturn(Optional.of(carrito));
        when(carritoRepo.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Carrito> resultado = service.eliminarItem("carrito-1", "prod-1");

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, resultado.get().getTotal());
    }

    @Test
    void eliminarItem_carritoNoExiste_devuelveVacio() {
        when(carritoRepo.findById("no-existe")).thenReturn(Optional.empty());

        Optional<Carrito> resultado = service.eliminarItem("no-existe", "prod-1");

        assertFalse(resultado.isPresent());
    }
}
