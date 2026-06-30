package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cl.duoc.monitoriza.dto.EstadisticaSensorDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.repository.SalaRepository;

class ResumenDiaServiceTest {

    @Mock
    private MedicionService medicionService;

    @Mock
    private SalaRepository salaRepository;

    private ResumenDiaService resumenDiaService;

    private static final LocalDate FECHA_HABIL = LocalDate.of(2026, 6, 17);
    private static final LocalDate SABADO = LocalDate.of(2026, 6, 20);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resumenDiaService = new ResumenDiaService(medicionService, salaRepository);
        when(salaRepository.findAll()).thenReturn(List.of());
    }

    @Test
    void construirResumen_sabado_devuelveDtoVacioControlado() {
        ResumenDiaDto resumen = resumenDiaService.construirResumen(SABADO);

        assertEquals(SABADO, resumen.getFecha());
        assertFalse(resumen.isDiaHabil());
        assertTrue(resumen.getEstadisticas().isEmpty());
        assertTrue(resumen.getBloques().isEmpty());
        assertTrue(resumen.getMensaje().contains("día hábil"));
    }

    @Test
    void construirResumen_diaHabil_calculaEstadisticasPorSensor() {
        Medicion m1 = medicion(20.0, 40.0, 35.0, 300.0, 400.0, 100.0);
        Medicion m2 = medicion(22.0, 60.0, 45.0, 500.0, 800.0, 220.0);
        when(medicionService.medicionesDelDia(FECHA_HABIL)).thenReturn(List.of(m1, m2));

        ResumenDiaDto resumen = resumenDiaService.construirResumen(FECHA_HABIL);

        assertTrue(resumen.isDiaHabil());
        assertEquals(2, resumen.getTotalLecturasJornada());
        assertEquals(6, resumen.getEstadisticas().size());

        EstadisticaSensorDto temp = resumen.getEstadisticas().get(0);
        assertEquals("Temperatura", temp.getSensor());
        assertEquals("°C", temp.getUnidad());
        assertEquals(20.0, temp.getMinimo());
        assertEquals(22.0, temp.getMaximo());
        assertEquals(21.0, temp.getPromedio());
    }

    @Test
    void construirResumen_diaHabilSinLecturas_devuelveEstadisticasVacias() {
        when(medicionService.medicionesDelDia(FECHA_HABIL)).thenReturn(List.of());

        ResumenDiaDto resumen = resumenDiaService.construirResumen(FECHA_HABIL);

        assertTrue(resumen.isDiaHabil());
        assertTrue(resumen.getEstadisticas().isEmpty());
        assertEquals(0, resumen.getTotalLecturasJornada());
    }

    private Medicion medicion(double t, double h, double db, double lux, double eco2, double tvoc) {
        Medicion m = new Medicion();
        m.setTemperatura(t);
        m.setHumedad(h);
        m.setDb(db);
        m.setLux(lux);
        m.setEco2(eco2);
        m.setTvoc(tvoc);
        m.setFechaHora(LocalDateTime.of(FECHA_HABIL, LocalTime.of(10, 0)));
        return m;
    }
}
