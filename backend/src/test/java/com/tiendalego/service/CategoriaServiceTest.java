package com.tiendalego.service;

import com.tiendalego.model.Categoria;
import com.tiendalego.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repo;

    @InjectMocks
    private CategoriaService service;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId("cat-1");
        categoria.setNombre("Star Wars");
        categoria.setDescripcion("Sets de la saga Star Wars");
        categoria.setActiva(true);
    }

    @Test
    void listarActivas_soloDevuelveActivas() {
        when(repo.findByActivaTrue()).thenReturn(List.of(categoria));

        List<Categoria> resultado = service.listarActivas();

        assertEquals(1, resultado.size());
        assertEquals("Star Wars", resultado.get(0).getNombre());
    }

    @Test
    void listarTodas_devuelveTodasLasCategorias() {
        Categoria inactiva = new Categoria();
        inactiva.setId("cat-2");
        inactiva.setNombre("Descontinuada");
        inactiva.setActiva(false);

        when(repo.findAll()).thenReturn(List.of(categoria, inactiva));

        List<Categoria> resultado = service.listarTodas();

        assertEquals(2, resultado.size());
    }

    @Test
    void buscarPorId_existente_devuelveCategoria() {
        when(repo.findById("cat-1")).thenReturn(Optional.of(categoria));

        Optional<Categoria> resultado = service.buscarPorId("cat-1");

        assertTrue(resultado.isPresent());
        assertEquals("Star Wars", resultado.get().getNombre());
    }

    @Test
    void crear_asignaActivaTrue() {
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria nueva = new Categoria();
        nueva.setNombre("Technic");
        nueva.setDescripcion("Sets mecanicos");

        Categoria resultado = service.crear(nueva);

        assertTrue(resultado.isActiva());
        assertNull(resultado.getId());
        verify(repo).save(any(Categoria.class));
    }

    @Test
    void actualizar_existente_modificaCampos() {
        Categoria datos = new Categoria();
        datos.setNombre("Star Wars Edicion Limitada");
        datos.setDescripcion("Edicion especial");
        datos.setActiva(true);

        when(repo.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Categoria> resultado = service.actualizar("cat-1", datos);

        assertTrue(resultado.isPresent());
        assertEquals("Star Wars Edicion Limitada", resultado.get().getNombre());
    }

    @Test
    void eliminar_existente_devuelveTrue() {
        when(repo.findById("cat-1")).thenReturn(Optional.of(categoria));

        assertTrue(service.eliminar("cat-1"));
        verify(repo).delete(categoria);
    }

    @Test
    void eliminar_inexistente_devuelveFalse() {
        when(repo.findById("no-existe")).thenReturn(Optional.empty());

        assertFalse(service.eliminar("no-existe"));
    }
}
