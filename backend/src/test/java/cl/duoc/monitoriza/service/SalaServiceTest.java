package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cl.duoc.monitoriza.model.Sala;
import cl.duoc.monitoriza.model.Institucion;
import cl.duoc.monitoriza.repository.SalaRepository;

class SalaServiceTest {

    @Mock
    private SalaRepository salaRepository;

    @InjectMocks
    private SalaService salaService;

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
        when(salaRepository.findAll()).thenReturn(salas);

        // Act
        List<Sala> result = salaService.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Sala 101", result.get(0).getNombre());
        assertEquals("Sala 102", result.get(1).getNombre());
        verify(salaRepository, times(1)).findAll();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(salaRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Sala> result = salaService.listar();

        // Assert
        assertEquals(0, result.size());
        verify(salaRepository, times(1)).findAll();
    }

    @Test
    void testCrear() {
        // Arrange
        when(salaRepository.save(sala)).thenReturn(sala);

        // Act
        Sala result = salaService.crear(sala);

        // Assert
        assertNotNull(result);
        assertEquals("Sala 101", result.getNombre());
        assertEquals(50.0, result.getM2());
        verify(salaRepository, times(1)).save(sala);
    }

    @Test
    void testCrearNewSala() {
        // Arrange
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        Sala newSala = new Sala(3L, "Sala 103", 70.0, 35, 6, inst, "false", "Natural", 2);
        when(salaRepository.save(newSala)).thenReturn(newSala);

        // Act
        Sala result = salaService.crear(newSala);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getIdSala());
        assertEquals("Sala 103", result.getNombre());
        verify(salaRepository, times(1)).save(newSala);
    }

    @Test
    void testActualizar() {
        // Arrange
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        Sala salaActualizada = new Sala(1L, "Sala 101 Actualizada", 55.0, 28, 5, inst, "false", "AC", 2);
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(salaRepository.save(sala)).thenReturn(sala);

        // Act
        Sala result = salaService.actualizar(1L, salaActualizada);

        // Assert
        assertNotNull(result);
        assertEquals(55.0, result.getM2());
        assertEquals(28, result.getCantidadEstudiantes());
        assertEquals(5, result.getCantidadVentanas());
        assertEquals("false", result.getAireAcondicionado());
        assertEquals("AC", result.getTipoDeVentilacion());
        assertEquals(2, result.getNumeroPiso());
        verify(salaRepository, times(1)).findById(1L);
        verify(salaRepository, times(1)).save(sala);
    }

    @Test
    void testActualizarNotFound() {
        // Arrange
        when(salaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            salaService.actualizar(999L, sala);
        });
        verify(salaRepository, times(1)).findById(999L);
    }

    @Test
    void testActualizarPartialFields() {
        // Arrange
        Institucion inst = new Institucion(1L, "Test Institution", "Address");
        Sala updates = new Sala(null, "Updated", 60.0, 30, 5, inst, "true", "Hybrid", 3);
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(salaRepository.save(sala)).thenReturn(sala);

        // Act
        Sala result = salaService.actualizar(1L, updates);

        // Assert
        assertNotNull(result);
        assertEquals(60.0, result.getM2());
        assertEquals(30, result.getCantidadEstudiantes());
        verify(salaRepository, times(1)).findById(1L);
    }
}
