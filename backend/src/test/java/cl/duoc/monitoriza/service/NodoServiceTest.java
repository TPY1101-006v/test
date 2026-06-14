package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cl.duoc.monitoriza.model.Nodo;
import cl.duoc.monitoriza.repository.NodoRepository;

class NodoServiceTest {

    @Mock
    private NodoRepository nodoRepository;

    @InjectMocks
    private NodoService nodoService;

    private Nodo nodo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nodo = new Nodo(1L, "Nodo Test", "Descripcion Test", null);
    }

    @Test
    void testListar() {
        // Arrange
        Nodo nodo2 = new Nodo(2L, "Nodo 2", "Descripcion 2", null);
        List<Nodo> nodos = Arrays.asList(nodo, nodo2);
        when(nodoRepository.findAll()).thenReturn(nodos);

        // Act
        List<Nodo> result = nodoService.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Descripcion Test", result.get(0).getDescripcion());
        assertEquals("Descripcion 2", result.get(1).getDescripcion());
        verify(nodoRepository, times(1)).findAll();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(nodoRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Nodo> result = nodoService.listar();

        // Assert
        assertEquals(0, result.size());
        verify(nodoRepository, times(1)).findAll();
    }

    @Test
    void testCrear() {
        // Arrange
        when(nodoRepository.save(nodo)).thenReturn(nodo);

        // Act
        Nodo result = nodoService.crear(nodo);

        // Assert
        assertNotNull(result);
        assertEquals("Descripcion Test", result.getDescripcion());
        verify(nodoRepository, times(1)).save(nodo);
    }

    @Test
    void testCrearNewNodo() {
        // Arrange
        Nodo newNodo = new Nodo(3L, "Nuevo Nodo", "Nueva Descripcion", null);
        when(nodoRepository.save(newNodo)).thenReturn(newNodo);

        // Act
        Nodo result = nodoService.crear(newNodo);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getIdNodo());
        assertEquals("Nueva Descripcion", result.getDescripcion());
        verify(nodoRepository, times(1)).save(newNodo);
    }
}
