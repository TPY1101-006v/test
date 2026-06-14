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

import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.service.MedicionService;

class MedicionControllerTest {

    @Mock
    private MedicionService medicionService;

    @InjectMocks
    private MedicionController medicionController;

    private Medicion medicion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        medicion = new Medicion();
        medicion.setIdMedicion(1L);
    }

    @Test
    void testListar() {
        // Arrange
        Medicion medicion2 = new Medicion();
        medicion2.setIdMedicion(2L);
        List<Medicion> mediciones = Arrays.asList(medicion, medicion2);
        when(medicionService.listar()).thenReturn(mediciones);

        // Act
        List<Medicion> result = medicionController.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getIdMedicion());
        assertEquals(2L, result.get(1).getIdMedicion());
        verify(medicionService, times(1)).listar();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(medicionService.listar()).thenReturn(Arrays.asList());

        // Act
        List<Medicion> result = medicionController.listar();

        // Assert
        assertEquals(0, result.size());
        verify(medicionService, times(1)).listar();
    }

    @Test
    void testCrear() {
        // Arrange
        when(medicionService.crear(medicion)).thenReturn(medicion);

        // Act
        Medicion result = medicionController.crear(medicion);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdMedicion());
        verify(medicionService, times(1)).crear(medicion);
    }

    @Test
    void testUltimas20() {
        // Arrange
        List<Medicion> mediciones = Arrays.asList(medicion);
        when(medicionService.ultimas20()).thenReturn(mediciones);

        // Act
        List<Medicion> result = medicionController.ultimas20();

        // Assert
        assertEquals(1, result.size());
        verify(medicionService, times(1)).ultimas20();
    }

    @Test
    void testUltimas20Empty() {
        // Arrange
        when(medicionService.ultimas20()).thenReturn(Arrays.asList());

        // Act
        List<Medicion> result = medicionController.ultimas20();

        // Assert
        assertEquals(0, result.size());
        verify(medicionService, times(1)).ultimas20();
    }

    @Test
    void testCrearNewMedicion() {
        // Arrange
        Medicion newMedicion = new Medicion();
        newMedicion.setIdMedicion(3L);
        when(medicionService.crear(newMedicion)).thenReturn(newMedicion);

        // Act
        Medicion result = medicionController.crear(newMedicion);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getIdMedicion());
        verify(medicionService, times(1)).crear(newMedicion);
    }
}
