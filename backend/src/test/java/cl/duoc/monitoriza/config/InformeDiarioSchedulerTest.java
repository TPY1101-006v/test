package cl.duoc.monitoriza.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import cl.duoc.monitoriza.service.InformeService;

class InformeDiarioSchedulerTest {

    @Mock
    private InformeService informeService;

    @InjectMocks
    private InformeDiarioScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scheduler, "informeDiarioActivo", true);
    }

    @Test
    void generarInformeDelDiaLlamaAlServicioEnDiaHabil() {
        LocalDate hoy = LocalDate.now();
        if (esFinDeSemana(hoy)) {
            scheduler.generarInformeDelDia();
            verify(informeService, never()).generarInforme(any());
            return;
        }

        scheduler.generarInformeDelDia();

        verify(informeService).generarInforme(hoy);
    }

    @Test
    void noGeneraSiEstaDesactivado() {
        ReflectionTestUtils.setField(scheduler, "informeDiarioActivo", false);

        scheduler.generarInformeDelDia();

        verify(informeService, never()).generarInforme(any());
    }

    @Test
    void informeDuplicadoNoPropagaExcepcion() {
        LocalDate hoy = LocalDate.now();
        if (esFinDeSemana(hoy)) {
            return;
        }

        doThrow(new IllegalStateException("Ya existe"))
                .when(informeService).generarInforme(hoy);

        scheduler.generarInformeDelDia();
    }

    private boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }
}
