package cl.duoc.monitoriza.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cl.duoc.monitoriza.model.Institucion;
import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.model.Nodo;
import cl.duoc.monitoriza.model.Sala;
import cl.duoc.monitoriza.repository.InstitucionRepository;
import cl.duoc.monitoriza.repository.MedicionRepository;
import cl.duoc.monitoriza.repository.NodoRepository;
import cl.duoc.monitoriza.repository.SalaRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final InstitucionRepository institucionRepository;
    private final SalaRepository salaRepository;
    private final NodoRepository nodoRepository;
    private final MedicionRepository medicionRepository;
    
    @Value("${app.seed.historico.enabled:true}")
    private boolean historicoEnabled;
    @Value("${app.seed.historico.desde}")
    private String desde;
    @Value("${app.seed.historico.hasta}")
    private String hasta;
    @Value("${app.seed.historico.intervalo-minutos:5}")
    private int intervaloMinutos;
    @Value("${app.seed.historico.hora-inicio:08:00}")
    private String horaInicio;
    @Value("${app.seed.historico.hora-fin:16:40}")
    private String horaFin;
    @Value("${app.seed.historico.solo-laboral:true}")
    private boolean soloLaboral;

    public DataInitializer(InstitucionRepository institucionRepository, SalaRepository salaRepository, NodoRepository nodoRepository, MedicionRepository medicionRepository) {
        this.institucionRepository = institucionRepository;
        this.salaRepository = salaRepository;
        this.nodoRepository = nodoRepository;  
        this.medicionRepository = medicionRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        Nodo nodo;

        if (nodoRepository.count()==0){
            Institucion institucion = new Institucion("Liceo Politécnico San Joaquín", "Av. San Joaquín 123, San Joaquín, Chile, colegio particular subvencionado");
            institucionRepository.save(institucion);
    
            Sala sala = new Sala("2° medio A", 50.0, 30, 6, institucion, "no", "cruzada", 1);
            salaRepository.save(sala);
    
            nodo = new Nodo("Nodo 1 - Sala: 2° medio A", "Nodo de medición de temperatura, humedad, db, lux, eco2 y tvoc", sala);
            nodoRepository.save(nodo);
        } else {
            nodo=nodoRepository.findAll().get(0);
            System.out.println("[Seed] Datos iniciales ya existen.");
        }

        if (medicionRepository.count() == 0 && historicoEnabled) {
            int total = generarHistoricoMensual(nodo);
            System.out.println("[Seed] Histórico mensual creado: " + total + " mediciones.");
        } else {
            System.out.println("[Seed] Datos iniciales ya existen.");
        }
    }
    
    private int generarHistoricoMensual(Nodo nodo) {
        LocalDate fechaDesde = LocalDate.parse(desde);
        LocalDate fechaHasta = LocalDate.parse(hasta);
        LocalTime inicioDia = LocalTime.parse(horaInicio);
        LocalTime finDia = LocalTime.parse(horaFin);

        List<Medicion> lote = new ArrayList<>();
        int total = 0;
        Random random = new Random(42);

        for (LocalDate dia = fechaDesde; !dia.isAfter(fechaHasta); dia = dia.plusDays(1)) {
            if (soloLaboral && esFinDeSemana(dia)) {
                continue;
            }

            LocalDateTime momento = dia.atTime(inicioDia);
            LocalDateTime finMomento = dia.atTime(finDia);

            while (!momento.isAfter(finMomento)) {
                if (dia.equals(LocalDate.now()) && momento.isAfter(LocalDateTime.now())) {
                    break;
                }
            
                Medicion m = generarMedicionParaMomento(momento, nodo, random);
                m.setFechaHora(momento);
                lote.add(m);
                total++;
            
                if (lote.size() >= 500) {
                    medicionRepository.saveAll(lote);
                    lote.clear();
                }
            
                momento = momento.plusMinutes(intervaloMinutos);  // ← imprescindible
            }
        }

        if (!lote.isEmpty()) {
            medicionRepository.saveAll(lote);
        }

        return total;
    }

    private Medicion generarMedicionParaMomento(LocalDateTime t, Nodo nodo, Random random) {
        int hora = t.getHour();

        double factorClase;
        if (hora >= 9 && hora < 12) {
            factorClase = 1.0;
        } else if (hora >= 13 && hora < 16) {
            factorClase = 0.9;
        } else if (hora == 12) {
            factorClase = 0.5;
        } else {
            factorClase = 0.6;
        }

        double temperatura = redondear1(23.6 + (hora - 8) * 0.12 + ruido(random, -0.4, 0.4));
        double humedad     = redondear1(38.0 + factorClase * ruido(random, 0, 8) + ruido(random, -2, 2));
        double db          = redondear1(32.0 + factorClase * ruido(random, 0, 14));
        double lux         = Math.round(300 + factorClase * ruido(random, 0, 140));
        double eco2        = Math.round(460 + factorClase * ruido(random, 0, 260));
        
        // ✨ CAMBIO: Ahora TVOC usa Math.round y da valores en ppb (ej. 150 a 300)
        double tvoc        = Math.round(150 + factorClase * ruido(random, 0, 150));

        if (random.nextDouble() < 0.06) {
            return inyectarOutlier(temperatura, humedad, db, lux, eco2, tvoc, random, nodo);
        }

        return new Medicion(temperatura, humedad, db, lux, eco2, tvoc, nodo);
    }

    private Medicion inyectarOutlier(
            double temperatura, double humedad, double db, double lux,
            double eco2, double tvoc, Random random, Nodo nodo) {

        int tipo = random.nextInt(6);

        switch (tipo) {
            case 0 -> temperatura = redondear1(27.0 + ruido(random, 0, 1.5));
            case 1 -> humedad = redondear1(51.0 + ruido(random, 0, 4));
            case 2 -> db = redondear1(52.0 + ruido(random, 0, 6));
            case 3 -> lux = Math.round(250 + ruido(random, 0, 40));
            case 4 -> eco2 = Math.round(820 + ruido(random, 0, 120));
            // ✨ CAMBIO: Alerta crítica de TVOC (arriba de 500)
            case 5 -> tvoc = Math.round(550 + ruido(random, 0, 100));
        }

        return new Medicion(temperatura, humedad, db, lux, eco2, tvoc, nodo);
    }

    private boolean esFinDeSemana(LocalDate dia) {
        DayOfWeek d = dia.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private double ruido(Random random, double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private double redondear1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    // Nota: Dejé esta función porque quizá la usa otro módulo, aunque ya no la usamos en TVOC
    private double redondear2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}