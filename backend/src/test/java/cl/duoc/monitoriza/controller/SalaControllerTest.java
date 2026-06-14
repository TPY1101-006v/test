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

import cl.duoc.monitoriza.model.Sala;
import cl.duoc.monitoriza.model.Institucion;
import cl.duoc.monitoriza.service.SalaService;

class SalaControllerTest {

    @Mock
    private SalaService salaService;

    @InjectMocks
    private SalaController salaController;

    private Sala sala;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        sala = new Sala(1L, "Sala 101", 50.0, 25, 4, inst, "true", "Natural", 1);
    }

    @Test
    void testListar() {
        // Arrange
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        Sala sala2 = new Sala(2L, "Sala 102", 60.0, 30, 5, inst, "true", "AC", 1);
        List<Sala> salas = Arrays.asList(sala, sala2);
        when(salaService.listar()).thenReturn(salas);

        // Act
        List<Sala> result = salaController.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Sala 101", result.get(0).getNombre());
        assertEquals("Sala 102", result.get(1).getNombre());
        verify(salaService, times(1)).listar();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(salaService.listar()).thenReturn(Arrays.asList());

        // Act
        List<Sala> result = salaController.listar();

        // Assert
        assertEquals(0, result.size());
        verify(salaService, times(1)).listar();
    }

    @Test
    void testCrear() {
        // Arrange
        when(salaService.crear(sala)).thenReturn(sala);

        // Act
        Sala result = salaController.crear(sala);

        // Assert
        assertNotNull(result);
        assertEquals("Sala 101", result.getNombre());
        assertEquals(50.0, result.getM2());
        verify(salaService, times(1)).crear(sala);
    }

    @Test
    void testCrearNewSala() {
        // Arrange
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        Sala newSala = new Sala(3L, "Sala 103", 70.0, 35, 6, inst, "false", "Natural", 2);
        when(salaService.crear(newSala)).thenReturn(newSala);

        // Act
        Sala result = salaController.crear(newSala);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getIdSala());
        assertEquals("Sala 103", result.getNombre());
        verify(salaService, times(1)).crear(newSala);
    }

    @Test
    void testActualizar() {
        // Arrange
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        Sala salaActualizada = new Sala(1L, "Sala 101 Updated", 55.0, 28, 5, inst, "false", "AC", 2);
        when(salaService.actualizar(1L, salaActualizada)).thenReturn(salaActualizada);

        // Act
        Sala result = salaController.actualizar(1L, salaActualizada);

        // Assert
        assertNotNull(result);
        assertEquals(55.0, result.getM2());
        assertEquals(28, result.getCantidadEstudiantes());
        verify(salaService, times(1)).actualizar(1L, salaActualizada);
    }

    @Test
    void testActualizarCallsService() {
        // Arrange
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        Sala salaActualizada = new Sala(1L, "Updated", 60.0, 30, 5, inst, "true", "Hybrid", 3);
        when(salaService.actualizar(1L, salaActualizada)).thenReturn(salaActualizada);

        // Act
        salaController.actualizar(1L, salaActualizada);

        // Assert
        verify(salaService).actualizar(1L, salaActualizada);
    }
}
