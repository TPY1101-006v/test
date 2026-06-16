package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cl.duoc.monitoriza.dto.AlteracionSensorDto;
import cl.duoc.monitoriza.dto.GeminiInformeResponseDto;
import cl.duoc.monitoriza.dto.InformeDetalleDto;
import cl.duoc.monitoriza.dto.InformeListadoDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.model.Informe;
import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.repository.InformeRepository;

class InformeServiceTest {

    @Mock
    private InformeRepository informeRepository;

    @Mock
    private ResumenDiaService resumenDiaService;

    @Mock
    private MedicionService medicionService;

    @Mock
    private GeminiService geminiService;

    @Mock
    private InformePdfService informePdfService;

    private ObjectMapper objectMapper;
    private InformeService informeService;

    private static final LocalDate FECHA_HABIL = LocalDate.of(2026, 6, 12);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        informeService = new InformeService(
                informeRepository, resumenDiaService, medicionService,
                geminiService, informePdfService, objectMapper);
        when(geminiService.tieneApiKeyConfigurada()).thenReturn(true);
    }

    @Test
    void listarDevuelveInformesOrdenados() {
        Informe informe = informeGuardado(1L, FECHA_HABIL);
        when(informeRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(informe));

        List<InformeListadoDto> result = informeService.listar();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(FECHA_HABIL, result.get(0).getFecha());
        assertFalse(result.get(0).isTienePdf());
    }

    @Test
    void buscarPorIdExistente() {
        Informe informe = informeGuardado(2L, FECHA_HABIL);
        when(informeRepository.findById(2L)).thenReturn(Optional.of(informe));

        InformeDetalleDto result = informeService.buscarPorId(2L);

        assertEquals(2L, result.getId());
        assertEquals(FECHA_HABIL, result.getFecha());
        assertNotNull(result.getContenidoJson());
        assertFalse(result.getContenidoJson().isBlank());
    }

    @Test
    void buscarPorIdInexistente() {
        when(informeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> informeService.buscarPorId(99L));
    }

    @Test
    void obtenerPdfExistente() {
        Informe informe = informeGuardado(1L, FECHA_HABIL);
        informe.setPdfGenerado(new byte[] { 1, 2, 3 });
        when(informeRepository.findById(1L)).thenReturn(Optional.of(informe));

        byte[] pdf = informeService.obtenerPdf(1L);

        assertEquals(3, pdf.length);
    }

    @Test
    void obtenerPdfSinArchivo() {
        Informe informe = informeGuardado(1L, FECHA_HABIL);
        when(informeRepository.findById(1L)).thenReturn(Optional.of(informe));

        assertThrows(IllegalStateException.class, () -> informeService.obtenerPdf(1L));
    }

    @Test
    void generarInformeDiaHabil() {
        ResumenDiaDto resumen = new ResumenDiaDto();
        resumen.setFecha(FECHA_HABIL);
        resumen.setTotalLecturasClase(10);

        GeminiInformeResponseDto analisis = new GeminiInformeResponseDto();
        analisis.setSolucionCortoPlazo("Abrir ventanas");
        analisis.setSolucionLargoPlazo("Instalar ventilacion mecanica");

        when(informeRepository.existsByFecha(FECHA_HABIL)).thenReturn(false);
        when(resumenDiaService.construirResumen(FECHA_HABIL)).thenReturn(resumen);
        when(geminiService.generarAnalisis(resumen)).thenReturn(analisis);
        when(informePdfService.generarPdf(resumen, analisis)).thenReturn(new byte[] { 37, 80, 68, 70 });
        when(informeRepository.save(any(Informe.class))).thenAnswer(inv -> {
            Informe guardado = inv.getArgument(0);
            guardado.setId(5L);
            return guardado;
        });

        InformeDetalleDto result = informeService.generarInforme(FECHA_HABIL);

        assertEquals(5L, result.getId());
        assertEquals(FECHA_HABIL, result.getFecha());
        assertNotNull(result.getContenidoJson());
        assertNotNull(result.getAnalisisJson());
        assertTrue(result.getAnalisisJson().contains("Abrir ventanas"));
        assertTrue(result.isTienePdf());

        ArgumentCaptor<Informe> captor = ArgumentCaptor.forClass(Informe.class);
        verify(informeRepository).save(captor.capture());
        assertEquals(FECHA_HABIL, captor.getValue().getFecha());
        assertNotNull(captor.getValue().getAnalisisJson());
        assertNotNull(captor.getValue().getPdfGenerado());
        verify(geminiService).generarAnalisis(resumen);
        verify(informePdfService).generarPdf(resumen, analisis);
    }

    @Test
    void generarInformeFinDeSemana() {
        LocalDate sabado = LocalDate.of(2026, 6, 13);

        assertThrows(IllegalArgumentException.class, () -> informeService.generarInforme(sabado));
        verify(informeRepository, never()).save(any());
        verify(geminiService, never()).generarAnalisis(any());
    }

    @Test
    void generarInformesPendientesSoloDiasConMediciones() {
        LocalDate dia1 = LocalDate.of(2026, 6, 10);
        LocalDate dia2 = LocalDate.of(2026, 6, 11);
        LocalDate dia3 = LocalDate.of(2026, 6, 12);
        LocalDate sabado = LocalDate.of(2026, 6, 13);

        when(informeRepository.existsByFecha(any())).thenReturn(false);
        when(medicionService.medicionesDelDia(dia1)).thenReturn(List.of(new Medicion()));
        when(medicionService.medicionesDelDia(dia2)).thenReturn(List.of());
        when(medicionService.medicionesDelDia(dia3)).thenReturn(List.of(new Medicion()));
        when(medicionService.medicionesDelDia(sabado)).thenReturn(List.of(new Medicion()));

        ResumenDiaDto resumen = new ResumenDiaDto();
        GeminiInformeResponseDto analisis = new GeminiInformeResponseDto();
        when(resumenDiaService.construirResumen(any())).thenReturn(resumen);
        when(geminiService.generarAnalisis(any())).thenReturn(analisis);
        when(informePdfService.generarPdf(any(), any())).thenReturn(new byte[] { 37, 80, 68, 70 });
        when(informeRepository.save(any(Informe.class))).thenAnswer(inv -> inv.getArgument(0));

        int generados = informeService.generarInformesPendientes(dia1, sabado);

        assertEquals(2, generados);
        verify(geminiService, times(2)).generarAnalisis(any());
        verify(medicionService, never()).medicionesDelDia(sabado);
    }

    @Test
    void generarInformeDuplicado() {
        when(informeRepository.existsByFecha(FECHA_HABIL)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> informeService.generarInforme(FECHA_HABIL));
        verify(resumenDiaService, never()).construirResumen(any());
        verify(geminiService, never()).generarAnalisis(any());
    }

    @Test
    void generarInformesPendientesSinApiKeyNoIntentaGenerar() {
        when(geminiService.tieneApiKeyConfigurada()).thenReturn(false);

        int generados = informeService.generarInformesPendientes(FECHA_HABIL, FECHA_HABIL);

        assertEquals(0, generados);
        verify(geminiService, never()).generarAnalisis(any());
        verify(medicionService, never()).medicionesDelDia(any());
    }

    @Test
    void generarInformesPendientesDetieneSiFallaApiKey() {
        LocalDate dia1 = LocalDate.of(2026, 6, 10);
        LocalDate dia2 = LocalDate.of(2026, 6, 11);

        when(informeRepository.existsByFecha(any())).thenReturn(false);
        when(medicionService.medicionesDelDia(dia1)).thenReturn(List.of(new Medicion()));
        when(medicionService.medicionesDelDia(dia2)).thenReturn(List.of(new Medicion()));
        when(resumenDiaService.construirResumen(any())).thenReturn(new ResumenDiaDto());
        when(geminiService.generarAnalisis(any())).thenThrow(new IllegalStateException(
                "Error llamando a Gemini (403): API key not valid. Please pass a valid API key."));

        int generados = informeService.generarInformesPendientes(dia1, dia2);

        assertEquals(0, generados);
        verify(geminiService, times(1)).generarAnalisis(any());
        verify(informeRepository, never()).save(any());
    }

    private Informe informeGuardado(Long id, LocalDate fecha) {
        ResumenDiaDto resumen = new ResumenDiaDto();
        resumen.setFecha(fecha);

        Informe informe = new Informe();
        informe.setId(id);
        informe.setFecha(fecha);
        try {
            informe.setContenidoJson(objectMapper.writeValueAsString(resumen));
            informe.setAnalisisJson(objectMapper.writeValueAsString(new GeminiInformeResponseDto()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        informe.setFechaGeneracion(LocalDateTime.of(2026, 6, 12, 16, 35));
        return informe;
    }
}
