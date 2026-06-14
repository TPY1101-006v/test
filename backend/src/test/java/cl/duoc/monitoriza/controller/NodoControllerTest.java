package cl.duoc.monitoriza.controller;

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
import cl.duoc.monitoriza.service.NodoService;

class NodoControllerTest {

    @Mock
    private NodoService nodoService;

    @InjectMocks
    private NodoController nodoController;

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
        when(nodoService.listar()).thenReturn(nodos);

        // Act
        List<Nodo> result = nodoController.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Descripcion Test", result.get(0).getDescripcion());
        assertEquals("Descripcion 2", result.get(1).getDescripcion());
        verify(nodoService, times(1)).listar();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(nodoService.listar()).thenReturn(Arrays.asList());

        // Act
        List<Nodo> result = nodoController.listar();

        // Assert
        assertEquals(0, result.size());
        verify(nodoService, times(1)).listar();
    }

    @Test
    void testCrear() {
        // Arrange
        when(nodoService.crear(nodo)).thenReturn(nodo);

        // Act
        Nodo result = nodoController.crear(nodo);

        // Assert
        assertNotNull(result);
        assertEquals("Descripcion Test", result.getDescripcion());
        verify(nodoService, times(1)).crear(nodo);
    }

    @Test
    void testCrearNewNodo() {
        // Arrange
        Nodo newNodo = new Nodo(3L, "Nuevo Nodo", "Nueva Descripcion", null);
        when(nodoService.crear(newNodo)).thenReturn(newNodo);

        // Act
        Nodo result = nodoController.crear(newNodo);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getIdNodo());
        assertEquals("Nueva Descripcion", result.getDescripcion());
        verify(nodoService, times(1)).crear(newNodo);
    }

    @Test
    void testCrearCallsService() {
        // Arrange
        when(nodoService.crear(nodo)).thenReturn(nodo);

        // Act
        nodoController.crear(nodo);

        // Assert
        verify(nodoService).crear(nodo);
    }
}
