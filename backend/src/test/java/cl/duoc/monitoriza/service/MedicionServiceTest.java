package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.repository.MedicionRepository;

class MedicionServiceTest {

    @Mock
    private MedicionRepository medicionRepository;

    @InjectMocks
    private MedicionService medicionService;

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
        when(medicionRepository.findAll()).thenReturn(mediciones);

        // Act
        List<Medicion> result = medicionService.listar();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getIdMedicion());
        assertEquals(2L, result.get(1).getIdMedicion());
        verify(medicionRepository, times(1)).findAll();
    }

    @Test
    void testListarEmpty() {
        // Arrange
        when(medicionRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Medicion> result = medicionService.listar();

        // Assert
        assertEquals(0, result.size());
        verify(medicionRepository, times(1)).findAll();
    }

    @Test
    void testUltimas20() {
        // Arrange
        List<Medicion> mediciones = Arrays.asList(medicion);
        when(medicionRepository.findTop20ByOrderByFechaHoraDesc()).thenReturn(mediciones);

        // Act
        List<Medicion> result = medicionService.ultimas20();

        // Assert
        assertEquals(1, result.size());
        verify(medicionRepository, times(1)).findTop20ByOrderByFechaHoraDesc();
    }

    @Test
    void testUltimas20Empty() {
        // Arrange
        when(medicionRepository.findTop20ByOrderByFechaHoraDesc()).thenReturn(Arrays.asList());

        // Act
        List<Medicion> result = medicionService.ultimas20();

        // Assert
        assertEquals(0, result.size());
        verify(medicionRepository, times(1)).findTop20ByOrderByFechaHoraDesc();
    }

    @Test
    void testCrear() {
        // Arrange
        when(medicionRepository.save(medicion)).thenReturn(medicion);

        // Act
        Medicion result = medicionService.crear(medicion);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdMedicion());
        verify(medicionRepository, times(1)).save(medicion);
    }

    @Test
    void testCrearNewMedicion() {
        // Arrange
        Medicion newMedicion = new Medicion();
        newMedicion.setIdMedicion(3L);
        when(medicionRepository.save(newMedicion)).thenReturn(newMedicion);

        // Act
        Medicion result = medicionService.crear(newMedicion);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getIdMedicion());
        verify(medicionRepository, times(1)).save(newMedicion);
    }
}
