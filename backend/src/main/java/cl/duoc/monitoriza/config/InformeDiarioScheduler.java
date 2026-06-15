package cl.duoc.monitoriza.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cl.duoc.monitoriza.service.InformeService;
import cl.duoc.monitoriza.util.HorarioEscolarUtil;

@Component
public class InformeDiarioScheduler {

    private static final Logger log = LoggerFactory.getLogger(InformeDiarioScheduler.class);

    private final InformeService informeService;

    @Value("${app.informe.diario.enabled:true}")
    private boolean informeDiarioActivo;

    public InformeDiarioScheduler(InformeService informeService) {
        this.informeService = informeService;
    }

    /**
     * Genera el informe del día a las 16:35, de lunes a viernes,
     * después del cierre de la jornada escolar (7:30–16:30).
     */
    @Scheduled(cron = "0 35 16 * * MON-FRI")
    public void generarInformeDelDia() {
        if (!informeDiarioActivo) {
            return;
        }

        LocalDate hoy = LocalDate.now();
        if (!HorarioEscolarUtil.esDiaHabil(hoy)) {
            return;
        }

        try {
            informeService.generarInforme(hoy);
            log.info("[InformeDiario] Informe generado para {}", hoy);
        } catch (IllegalStateException e) {
            log.info("[InformeDiario] Informe ya existente para {}: {}", hoy, e.getMessage());
        } catch (Exception e) {
            log.error("[InformeDiario] Error generando informe para {}: {}", hoy, e.getMessage(), e);
        }
    }
}
