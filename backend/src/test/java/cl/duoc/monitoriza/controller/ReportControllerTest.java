package cl.duoc.monitoriza.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
import cl.duoc.monitoriza.model.Sala;
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
        Sala sala = new Sala("2 medio A", 50.0, 30, 6, null, "no", "cruzada", 1);
        nodo = new Nodo(1L, "Nodo Principal", "Sensor aula", sala);
        medicion = new Medicion();
        medicion.setId(1L);
        medicion.setNodo(nodo);
        medicion.setTemperatura(25.5);
        medicion.setHumedad(60.0);
        medicion.setDb(65.0);
        medicion.setLux(500.0);
        medicion.setEco2(450.0);
        medicion.setTvoc(100.0);
        medicion.setFechaHora(LocalDateTime.of(2026, 6, 12, 10, 30));
    }

    private String csvBody(ResponseEntity<byte[]> result) {
        byte[] body = result.getBody();
        assertNotNull(body);
        assertTrue(body.length > 3);
        return new String(body, 3, body.length - 3, StandardCharsets.UTF_8);
    }

    @Test
    void testDescargarCSV() {
        List<Medicion> mediciones = Arrays.asList(medicion);
        when(medicionRepository.findAllByOrderByFechaHoraAsc()).thenReturn(mediciones);

        ResponseEntity<byte[]> result = reportController.descargarCSV();

        assertNotNull(result.getBody());
        String csvContent = csvBody(result);
        assertTrue(csvContent.contains("MONITORIZA"));
        assertTrue(csvContent.contains("Temperatura (C)"));
        assertTrue(csvContent.contains("25.5"));
        assertTrue(csvContent.contains("Nodo Principal"));
    }

    @Test
    void testDescargarCSVEmpty() {
        when(medicionRepository.findAllByOrderByFechaHoraAsc()).thenReturn(new ArrayList<>());

        ResponseEntity<byte[]> result = reportController.descargarCSV();

        String csvContent = csvBody(result);
        assertTrue(csvContent.contains("Total de lecturas;0"));
        assertTrue(csvContent.contains("Temperatura (C)"));
    }

    @Test
    void testDescargarCSVMultipleMediciones() {
        Medicion medicion2 = new Medicion();
        medicion2.setId(2L);
        medicion2.setNodo(nodo);
        medicion2.setTemperatura(26.0);
        medicion2.setHumedad(65.0);
        medicion2.setDb(66.0);
        medicion2.setLux(510.0);
        medicion2.setEco2(460.0);
        medicion2.setTvoc(110.0);
        medicion2.setFechaHora(LocalDateTime.of(2026, 6, 12, 11, 0));

        when(medicionRepository.findAllByOrderByFechaHoraAsc())
                .thenReturn(Arrays.asList(medicion, medicion2));

        ResponseEntity<byte[]> result = reportController.descargarCSV();

        String csvContent = csvBody(result);
        assertTrue(csvContent.contains("25.5"));
        assertTrue(csvContent.contains("26.0"));
    }

    @Test
    void testDescargarCSVWithNullNodo() {
        Medicion medicionSinNodo = new Medicion();
        medicionSinNodo.setId(3L);
        medicionSinNodo.setTemperatura(25.0);

        when(medicionRepository.findAllByOrderByFechaHoraAsc())
                .thenReturn(Arrays.asList(medicionSinNodo));

        ResponseEntity<byte[]> result = reportController.descargarCSV();

        String csvContent = csvBody(result);
        assertTrue(csvContent.contains("Total de lecturas;1"));
        assertTrue(!csvContent.contains("25.0") || csvContent.lines().filter(l -> l.startsWith("12/")).count() == 0);
    }

    @Test
    void testDescargarCSVWithNullFechaHora() {
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

        when(medicionRepository.findAllByOrderByFechaHoraAsc())
                .thenReturn(Arrays.asList(medicionSinFecha));

        ResponseEntity<byte[]> result = reportController.descargarCSV();

        String csvContent = csvBody(result);
        assertTrue(csvContent.contains("1;Nodo Principal"));
    }

    @Test
    void testDescargarCSVHeadersPresent() {
        when(medicionRepository.findAllByOrderByFechaHoraAsc()).thenReturn(Arrays.asList(medicion));

        ResponseEntity<byte[]> result = reportController.descargarCSV();

        assertNotNull(result.getHeaders());
        assertTrue(result.getHeaders().getContentDisposition().toString()
                .contains("attachment"));
        assertTrue(result.getHeaders().getContentDisposition().getFilename()
                .contains("monitoriza-mediciones"));
    }

    @Test
    void testObtenerMetadata() {
        when(medicionRepository.count()).thenReturn(2376L);
        when(medicionRepository.findFirstByOrderByFechaHoraAsc()).thenReturn(medicion);
        when(medicionRepository.findFirstByOrderByFechaHoraDesc()).thenReturn(medicion);

        var meta = reportController.obtenerMetadata();

        assertTrue(meta.getTotalMediciones() == 2376L);
        assertNotNull(meta.getFechaDesde());
        assertNotNull(meta.getDescripcion());
    }
}
