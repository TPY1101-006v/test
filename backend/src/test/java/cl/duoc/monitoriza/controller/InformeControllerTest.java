package cl.duoc.monitoriza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.duoc.monitoriza.dto.InformeDetalleDto;
import cl.duoc.monitoriza.dto.InformeListadoDto;
import cl.duoc.monitoriza.service.InformeService;

class InformeControllerTest {

    @Mock
    private InformeService informeService;

    @InjectMocks
    private InformeController informeController;

    private InformeListadoDto listadoDto;
    private InformeDetalleDto detalleDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        listadoDto = new InformeListadoDto();
        listadoDto.setId(1L);
        listadoDto.setFecha(LocalDate.of(2026, 6, 12));
        listadoDto.setFechaGeneracion(LocalDateTime.of(2026, 6, 12, 16, 35));

        detalleDto = new InformeDetalleDto();
        detalleDto.setId(1L);
        detalleDto.setFecha(LocalDate.of(2026, 6, 12));
        detalleDto.setContenidoJson("{\"fecha\":\"2026-06-12\"}");
    }

    @Test
    void listar() {
        when(informeService.listar()).thenReturn(List.of(listadoDto));

        List<InformeListadoDto> result = informeController.listar();

        assertEquals(1, result.size());
        verify(informeService, times(1)).listar();
    }

    @Test
    void buscarPorIdOk() {
        when(informeService.buscarPorId(1L)).thenReturn(detalleDto);

        ResponseEntity<?> response = informeController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void buscarPorIdNotFound() {
        when(informeService.buscarPorId(99L))
                .thenThrow(new IllegalArgumentException("Informe no encontrado: 99"));

        ResponseEntity<?> response = informeController.buscarPorId(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void generarOk() {
        LocalDate fecha = LocalDate.of(2026, 6, 12);
        when(informeService.generarInforme(fecha)).thenReturn(detalleDto);

        ResponseEntity<?> response = informeController.generar(fecha);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(informeService, times(1)).generarInforme(fecha);
    }

    @Test
    void generarBadRequest() {
        LocalDate fecha = LocalDate.of(2026, 6, 13);
        when(informeService.generarInforme(fecha))
                .thenThrow(new IllegalArgumentException("Solo se generan informes de lunes a viernes"));

        ResponseEntity<?> response = informeController.generar(fecha);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Solo se generan informes de lunes a viernes", body.get("error"));
    }

    @Test
    void generarConflict() {
        LocalDate fecha = LocalDate.of(2026, 6, 12);
        when(informeService.generarInforme(fecha))
                .thenThrow(new IllegalStateException("Ya existe un informe"));

        ResponseEntity<?> response = informeController.generar(fecha);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void descargarPdfOk() {
        when(informeService.obtenerPdf(1L)).thenReturn(new byte[] { 37, 80, 68, 70 });

        ResponseEntity<?> response = informeController.descargarPdf(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void descargarPdfSinArchivo() {
        when(informeService.obtenerPdf(1L))
                .thenThrow(new IllegalStateException("El informe aún no tiene PDF generado"));

        ResponseEntity<?> response = informeController.descargarPdf(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void descargarPdfInformeInexistente() {
        when(informeService.obtenerPdf(99L))
                .thenThrow(new IllegalArgumentException("Informe no encontrado: 99"));

        ResponseEntity<?> response = informeController.descargarPdf(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
