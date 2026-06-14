package cl.duoc.monitoriza.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.model.Nodo;
import cl.duoc.monitoriza.repository.MedicionRepository;

class ReportControllerTest {

    @Mock
    private MedicionRepository medicionRepository;

    @InjectMocks
    private ReportController reportController;

    private Medicion medicion;
    private Nodo nodo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nodo = new Nodo(1L, "Nodo Principal");
        medicion = new Medicion();
        medicion.setId(1L);
        medicion.setNodo(nodo);
        medicion.setTemperatura(25.5);
        medicion.setHumedad(60.0);
        medicion.setDb(65.0);
        medicion.setLux(500.0);
        medicion.setEco2(450.0);
        medicion.setTvoc(100.0);
        medicion.setFechaHora(LocalDateTime.now());
    }

    @Test
    void testDescargarCSV() {
        // Arrange
        List<Medicion> mediciones = Arrays.asList(medicion);
        when(medicionRepository.findAll()).thenReturn(mediciones);

        // Act
        ResponseEntity<byte[]> result = reportController.descargarCSV();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertTrue(result.getBody().length > 0);
        String csvContent = new String(result.getBody());
        assertTrue(csvContent.contains("idNodo"));
        assertTrue(csvContent.contains("temperatura"));
    }

    @Test
    void testDescargarCSVEmpty() {
        // Arrange
        when(medicionRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<byte[]> result = reportController.descargarCSV();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBody());
        String csvContent = new String(result.getBody());
        assertTrue(csvContent.contains("idNodo,descripcionNodo"));
    }

    @Test
    void testDescargarCSVMultipleMediciones() {
        // Arrange
        Medicion medicion2 = new Medicion();
        medicion2.setId(2L);
        medicion2.setNodo(nodo);
        medicion2.setTemperatura(26.0);
        medicion2.setHumedad(65.0);
        medicion2.setDb(66.0);
        medicion2.setLux(510.0);
        medicion2.setEco2(460.0);
        medicion2.setTvoc(110.0);
        medicion2.setFechaHora(LocalDateTime.now());

        List<Medicion> mediciones = Arrays.asList(medicion, medicion2);
        when(medicionRepository.findAll()).thenReturn(mediciones);

        // Act
        ResponseEntity<byte[]> result = reportController.descargarCSV();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBody());
        String csvContent = new String(result.getBody());
        assertTrue(csvContent.contains("25.50"));
        assertTrue(csvContent.contains("26.00"));
    }

    @Test
    void testDescargarCSVWithNullNodo() {
        // Arrange
        Medicion medicionSinNodo = new Medicion();
        medicionSinNodo.setId(3L);
        medicionSinNodo.setNodo(null);
        medicionSinNodo.setTemperatura(25.0);

        List<Medicion> mediciones = Arrays.asList(medicionSinNodo);
        when(medicionRepository.findAll()).thenReturn(mediciones);

        // Act
        ResponseEntity<byte[]> result = reportController.descargarCSV();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBody());
        String csvContent = new String(result.getBody());
        // CSV should only have header since nodo is null
        assertTrue(csvContent.contains("idNodo,descripcionNodo"));
    }

    @Test
    void testDescargarCSVWithNullFechaHora() {
        // Arrange
        Medicion medicionSinFecha = new Medicion();
        medicionSinFecha.setId(1L);
        medicionSinFecha.setNodo(nodo);
        medicionSinFecha.setTemperatura(25.0);
        medicionSinFecha.setHumedad(60.0);
        medicionSinFecha.setDb(65.0);
        medicionSinFecha.setLux(500.0);
        medicionSinFecha.setEco2(450.0);
        medicionSinFecha.setTvoc(100.0);
        medicionSinFecha.setFechaHora(null);

        List<Medicion> mediciones = Arrays.asList(medicionSinFecha);
        when(medicionRepository.findAll()).thenReturn(mediciones);

        // Act
        ResponseEntity<byte[]> result = reportController.descargarCSV();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getBody());
        String csvContent = new String(result.getBody());
        assertTrue(csvContent.contains("1,Nodo Principal"));
    }

    @Test
    void testDescargarCSVHeadersPresent() {
        // Arrange
        when(medicionRepository.findAll()).thenReturn(Arrays.asList(medicion));

        // Act
        ResponseEntity<byte[]> result = reportController.descargarCSV();

        // Assert
        assertNotNull(result.getHeaders());
        assertTrue(result.getHeaders().getContentDisposition().toString()
                .contains("attachment"));
    }
}
