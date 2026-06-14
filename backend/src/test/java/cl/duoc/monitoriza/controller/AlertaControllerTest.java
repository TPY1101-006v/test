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

import cl.duoc.monitoriza.model.Alerta;
import cl.duoc.monitoriza.repository.AlertaRepository;

class AlertaControllerTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaController alertaController;

    private Alerta alerta;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alerta = new Alerta("Temperatura", 28.5, "°C", true);
        alerta.setId(1L);
    }

    @Test
    void testListarAlertas() {
        // Arrange
        Alerta alerta2 = new Alerta("Humedad", 75.0, "%", true);
        alerta2.setId(2L);
        List<Alerta> alertas = Arrays.asList(alerta, alerta2);
        when(alertaRepository.findTop15ByOrderByIdDesc()).thenReturn(alertas);

        // Act
        List<Alerta> result = alertaController.listarAlertas();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Temperatura", result.get(0).getSensor());
        assertEquals("Humedad", result.get(1).getSensor());
        verify(alertaRepository, times(1)).findTop15ByOrderByIdDesc();
    }

    @Test
    void testListarAlertasEmpty() {
        // Arrange
        when(alertaRepository.findTop15ByOrderByIdDesc()).thenReturn(Arrays.asList());

        // Act
        List<Alerta> result = alertaController.listarAlertas();

        // Assert
        assertEquals(0, result.size());
        verify(alertaRepository, times(1)).findTop15ByOrderByIdDesc();
    }

    @Test
    void testListarAlertasReturnsTop15() {
        // Arrange
        List<Alerta> alertas = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Alerta a = new Alerta("Sensor" + i, (double) i, "unit", true);
            a.setId((long) i);
            alertas.add(a);
        }
        when(alertaRepository.findTop15ByOrderByIdDesc()).thenReturn(alertas);

        // Act
        List<Alerta> result = alertaController.listarAlertas();

        // Assert
        assertEquals(15, result.size());
        verify(alertaRepository, times(1)).findTop15ByOrderByIdDesc();
    }

    @Test
    void testRegistrarAlerta() {
        // Arrange
        when(alertaRepository.save(alerta)).thenReturn(alerta);

        // Act
        Alerta result = alertaController.registrarAlerta(alerta);

        // Assert
        assertNotNull(result);
        assertEquals("Temperatura", result.getSensor());
        assertEquals(28.5, result.getValue());
        assertEquals("°C", result.getUnit());
        verify(alertaRepository, times(1)).save(alerta);
    }

    @Test
    void testRegistrarAlertaNewAlerta() {
        // Arrange
        Alerta newAlerta = new Alerta("CO2", 450.0, "ppm", false);
        when(alertaRepository.save(newAlerta)).thenReturn(newAlerta);

        // Act
        Alerta result = alertaController.registrarAlerta(newAlerta);

        // Assert
        assertNotNull(result);
        assertEquals("CO2", result.getSensor());
        assertEquals(450.0, result.getValue());
        verify(alertaRepository, times(1)).save(newAlerta);
    }

    @Test
    void testRegistrarAlertaCallsSave() {
        // Arrange
        when(alertaRepository.save(alerta)).thenReturn(alerta);

        // Act
        alertaController.registrarAlerta(alerta);

        // Assert
        verify(alertaRepository).save(alerta);
    }

    @Test
    void testRegistrarAlertaWithHighValue() {
        // Arrange
        Alerta highAlerta = new Alerta("Temperatura", 35.0, "°C", true);
        when(alertaRepository.save(highAlerta)).thenReturn(highAlerta);

        // Act
        Alerta result = alertaController.registrarAlerta(highAlerta);

        // Assert
        assertEquals(true, result.isHigh());
        assertEquals(35.0, result.getValue());
    }
}
