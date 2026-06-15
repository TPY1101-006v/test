package cl.duoc.monitoriza.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.monitoriza.dto.AlteracionSensorDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.model.Informe;
import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.repository.InformeRepository;
import cl.duoc.monitoriza.util.HorarioEscolarUtil;
import cl.duoc.monitoriza.util.RangosAmbientalesUtil;
import cl.duoc.monitoriza.util.RangosAmbientalesUtil.Sensor;

@Service
public class ChatContextBuilder {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final MedicionService medicionService;
    private final InformeRepository informeRepository;
    private final ObjectMapper objectMapper;
    private final int informesPatronesMax;

    public ChatContextBuilder(
            MedicionService medicionService,
            InformeRepository informeRepository,
            ObjectMapper objectMapper,
            @Value("${app.chat.informes-patrones-max:15}") int informesPatronesMax) {
        this.medicionService = medicionService;
        this.informeRepository = informeRepository;
        this.objectMapper = objectMapper;
        this.informesPatronesMax = informesPatronesMax;
    }

    public String construirContextoDia(LocalDate fecha) {
        if (!HorarioEscolarUtil.esDiaHabil(fecha)) {
            return "Fecha " + fecha + ": no es día hábil escolar.";
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = fecha.equals(LocalDate.now())
                ? min(ahora, HorarioEscolarUtil.finJornada(fecha))
                : HorarioEscolarUtil.finJornada(fecha);

        List<Medicion> delDia = medicionService.medicionesDelDia(fecha).stream()
                .filter(m -> m.getFechaHora() != null && !m.getFechaHora().isAfter(limite))
                .sorted(Comparator.comparing(Medicion::getFechaHora))
                .toList();

        if (delDia.isEmpty()) {
            return "Fecha " + fecha + ": sin mediciones registradas hasta " + HORA.format(limite) + ".";
        }

        Map<String, Integer> alteraciones = contarAlteraciones(delDia);
        Medicion ultima = delDia.get(delDia.size() - 1);

        StringBuilder sb = new StringBuilder();
        sb.append("Mediciones de ").append(fecha).append(" hasta ").append(HORA.format(limite)).append(".\n");
        sb.append("Total lecturas: ").append(delDia.size()).append(".\n");
        sb.append("Alteraciones acumuladas: ").append(formatearAlteraciones(alteraciones)).append(".\n");
        sb.append("Ultima lectura (").append(HORA.format(ultima.getFechaHora())).append("): ");
        sb.append("T=").append(fmt(ultima.getTemperatura())).append("C, ");
        sb.append("H=").append(fmt(ultima.getHumedad())).append("%, ");
        sb.append("dB=").append(fmt(ultima.getDb())).append(", ");
        sb.append("lux=").append(fmt0(ultima.getLux())).append(", ");
        sb.append("CO2=").append(fmt0(ultima.getEco2())).append("ppm, ");
        sb.append("TVOC=").append(fmt0(ultima.getTvoc())).append("ppb.");
        return sb.toString();
    }

    public String construirContextoPatrones() {
        List<Informe> informes = informeRepository.findAllByOrderByFechaDesc().stream()
                .limit(informesPatronesMax)
                .toList();

        if (informes.isEmpty()) {
            return "No hay informes historicos generados aun.";
        }

        Map<String, Integer> totales = new LinkedHashMap<>();
        int informesLeidos = 0;

        for (Informe informe : informes) {
            if (informe.getContenidoJson() == null || informe.getContenidoJson().isBlank()) {
                continue;
            }
            try {
                ResumenDiaDto resumen = objectMapper.readValue(informe.getContenidoJson(), ResumenDiaDto.class);
                if (resumen.getAlteracionesJornada() == null) {
                    continue;
                }
                for (AlteracionSensorDto alt : resumen.getAlteracionesJornada()) {
                    if (alt.getSensor() != null && alt.getVecesAlterada() > 0) {
                        totales.merge(alt.getSensor(), alt.getVecesAlterada(), Integer::sum);
                    }
                }
                informesLeidos++;
            } catch (JsonProcessingException ignored) {
                // omitir informes con JSON invalido
            }
        }

        if (informesLeidos == 0) {
            return "Hay informes guardados pero sin datos de alteraciones parseables.";
        }

        LocalDate desde = informes.get(informes.size() - 1).getFecha();
        LocalDate hasta = informes.get(0).getFecha();

        return "Patrones en " + informesLeidos + " informes (" + desde + " a " + hasta + "). "
                + "Alteraciones acumuladas por sensor: " + formatearAlteraciones(totales) + ".";
    }

    private Map<String, Integer> contarAlteraciones(List<Medicion> lecturas) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (Sensor sensor : Sensor.values()) {
            int count = (int) lecturas.stream()
                    .filter(m -> RangosAmbientalesUtil.estaAlterado(
                            sensor, RangosAmbientalesUtil.obtenerValor(m, sensor)))
                    .count();
            if (count > 0) {
                mapa.put(sensor.getEtiqueta(), count);
            }
        }
        return mapa;
    }

    private String formatearAlteraciones(Map<String, Integer> alteraciones) {
        if (alteraciones.isEmpty()) {
            return "ninguna";
        }
        return alteraciones.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    private String fmt(Double v) {
        return v == null ? "—" : String.format(Locale.US, "%.1f", v);
    }

    private String fmt0(Double v) {
        return v == null ? "—" : String.format(Locale.US, "%.0f", v);
    }
}
