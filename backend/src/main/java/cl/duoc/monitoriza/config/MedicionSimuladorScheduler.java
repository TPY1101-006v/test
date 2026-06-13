package cl.duoc.monitoriza.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.model.Nodo;
import cl.duoc.monitoriza.repository.MedicionRepository;
import cl.duoc.monitoriza.repository.NodoRepository;

@Component
public class MedicionSimuladorScheduler {

    private final NodoRepository nodoRepository;
    private final MedicionRepository medicionRepository;

    @Value("${app.simulador.mediciones.enabled:true}")
    private boolean simuladorActivo;

    public MedicionSimuladorScheduler(
            NodoRepository nodoRepository,
            MedicionRepository medicionRepository) {
        this.nodoRepository = nodoRepository;
        this.medicionRepository = medicionRepository;
    }

    /**
     * Inserta una medición ficticia cada 5 minutos (300 000 ms).
     * fixedRate = empieza cada 5 min desde que terminó la anterior.
     */
    @Scheduled(fixedRate = 300_000)
    public void agregarMedicionFicticia() {
        if (!simuladorActivo) {
            return;
        }

        if (nodoRepository.count() == 0) {
            System.out.println("[Simulador] No hay nodo en BD. Esperando DataInitializer...");
            return;
        }

        Nodo nodo = nodoRepository.findAll().get(0);
        Medicion medicion = generarMedicionFicticia(nodo);
        medicionRepository.save(medicion);

        System.out.println("[Simulador] Medición guardada: temp="
                + medicion.getTemperatura() + "°C, lux="
                + medicion.getLux() + " lx, "
                + medicion.getFechaHora());
    }

    private Medicion generarMedicionFicticia(Nodo nodo) {
        int hora = LocalDateTime.now().getHour();
        boolean enClase = hora >= 8 && hora < 17;

        double temperatura = redondear1(23.5 + (Math.random() * 2.5));       // 23.5 - 26.0
        double humedad     = redondear1(38.0 + (Math.random() * 10.0));      // 38 - 48
        double db          = redondear1(enClase ? 35.0 + Math.random() * 12 : 30.0 + Math.random() * 4);
        double lux         = Math.round(enClase ? 350 + Math.random() * 120 : 20 + Math.random() * 30);
        double eco2        = Math.round(enClase ? 550 + Math.random() * 250 : 420 + Math.random() * 80);
        
        // ✨ CAMBIO: Ahora TVOC genera valores entre 150-380 en clases y 100-150 fuera de horario
        double tvoc        = Math.round(enClase ? 150 + Math.random() * 230 : 100 + Math.random() * 50);

        Medicion m = new Medicion(temperatura, humedad, db, lux, eco2, tvoc, nodo);
        m.setFechaHora(LocalDateTime.now());
        return m;
    }

    private double redondear1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private double redondear2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}