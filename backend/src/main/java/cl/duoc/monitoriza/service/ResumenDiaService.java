package cl.duoc.monitoriza.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import org.springframework.stereotype.Service;

import cl.duoc.monitoriza.dto.AlteracionSensorDto;
import cl.duoc.monitoriza.dto.BloqueMedicionDto;
import cl.duoc.monitoriza.dto.EstadisticaSensorDto;
import cl.duoc.monitoriza.dto.LecturaInformeDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.dto.SalaInformeDto;
import cl.duoc.monitoriza.model.Medicion;
import cl.duoc.monitoriza.model.Sala;
import cl.duoc.monitoriza.repository.SalaRepository;
import cl.duoc.monitoriza.util.BloqueHorario;
import cl.duoc.monitoriza.util.HorarioEscolarUtil;
import cl.duoc.monitoriza.util.RangosAmbientalesUtil;
import cl.duoc.monitoriza.util.RangosAmbientalesUtil.Sensor;

@Service
public class ResumenDiaService {

    private static final DateTimeFormatter HORA_LECTURA = DateTimeFormatter.ofPattern("HH:mm");

    private final MedicionService medicionService;
    private final SalaRepository salaRepository;

    public ResumenDiaService(MedicionService medicionService, SalaRepository salaRepository) {
        this.medicionService = medicionService;
        this.salaRepository = salaRepository;
    }

    public ResumenDiaDto construirResumen(LocalDate fecha) {
        if (!HorarioEscolarUtil.esDiaHabil(fecha)) {
            return resumenDiaNoHabil(fecha);
        }

        List<Medicion> delDia = medicionService.medicionesDelDia(fecha);
        Map<BloqueHorario, List<Medicion>> agrupadas = HorarioEscolarUtil.agruparPorBloque(delDia);

        ResumenDiaDto resumen = new ResumenDiaDto();
        resumen.setFecha(fecha);
        resumen.setDiaHabil(true);
        resumen.setTotalLecturasJornada(delDia.size());
        resumen.setEstadisticas(calcularEstadisticas(delDia));
        resumen.setSala(obtenerSalaInforme());

        int totalClase = 0;
        int tempBloquesClase = 0;
        int humBloquesClase = 0;
        int dbBloquesClase = 0;
        int luxBloquesClase = 0;
        int eco2BloquesClase = 0;
        int tvocBloquesClase = 0;

        for (BloqueHorario bloque : BloqueHorario.values()) {
            List<Medicion> lecturas = agrupadas.getOrDefault(bloque, List.of()).stream()
                    .sorted(Comparator.comparing(Medicion::getFechaHora))
                    .toList();

            BloqueMedicionDto dto = new BloqueMedicionDto();
            dto.setNombre(bloque.getEtiqueta());
            dto.setTituloAmigable(bloque.getTituloAmigable());
            dto.setHorario(bloque.getEtiqueta());
            dto.setBloqueDeClase(bloque.isBloqueDeClase());
            dto.setCantidadLecturas(lecturas.size());
            dto.setLecturas(lecturas.stream().map(this::toLecturaDto).toList());

            if (lecturas.isEmpty()) {
                dto.setTemperatura("—");
                dto.setHumedad("—");
                dto.setDb("—");
                dto.setLux("—");
                dto.setEco2("—");
                dto.setTvoc("—");
                resumen.getBloques().add(dto);
                continue;
            }

            double t = promedio(lecturas, Medicion::getTemperatura);
            double h = promedio(lecturas, Medicion::getHumedad);
            double db = promedio(lecturas, Medicion::getDb);
            double lx = promedio(lecturas, Medicion::getLux);
            double co = promedio(lecturas, Medicion::getEco2);
            double tv = promedio(lecturas, Medicion::getTvoc);

            dto.setTemperatura(fmt1(t));
            dto.setHumedad(fmt1(h));
            dto.setDb(fmt1(db));
            dto.setLux(fmt0(lx));
            dto.setEco2(fmt0(co));
            dto.setTvoc(fmt0(tv));

            boolean tAlt = lecturas.stream()
                    .anyMatch(m -> RangosAmbientalesUtil.estaAlterado(Sensor.TEMPERATURA, m.getTemperatura()));
            boolean hAlt = lecturas.stream()
                    .anyMatch(m -> RangosAmbientalesUtil.estaAlterado(Sensor.HUMEDAD, m.getHumedad()));
            boolean dbAlt = lecturas.stream()
                    .anyMatch(m -> RangosAmbientalesUtil.estaAlterado(Sensor.DB, m.getDb()));
            boolean lxAlt = lecturas.stream()
                    .anyMatch(m -> RangosAmbientalesUtil.estaAlterado(Sensor.LUX, m.getLux()));
            boolean coAlt = lecturas.stream()
                    .anyMatch(m -> RangosAmbientalesUtil.estaAlterado(Sensor.ECO2, m.getEco2()));
            boolean tvAlt = lecturas.stream()
                    .anyMatch(m -> RangosAmbientalesUtil.estaAlterado(Sensor.TVOC, m.getTvoc()));

            dto.setTemperaturaAlterada(tAlt);
            dto.setHumedadAlterada(hAlt);
            dto.setDbAlterada(dbAlt);
            dto.setLuxAlterada(lxAlt);
            dto.setEco2Alterada(coAlt);
            dto.setTvocAlterada(tvAlt);

            if (bloque.isBloqueDeClase()) {
                totalClase += lecturas.size();
                if (tAlt) tempBloquesClase++;
                if (hAlt) humBloquesClase++;
                if (dbAlt) dbBloquesClase++;
                if (lxAlt) luxBloquesClase++;
                if (coAlt) eco2BloquesClase++;
                if (tvAlt) tvocBloquesClase++;
            }

            resumen.getBloques().add(dto);
        }

        resumen.setTotalLecturasClase(totalClase);

        agregarSiAlterado(resumen.getAlteraciones(), Sensor.TEMPERATURA, tempBloquesClase);
        agregarSiAlterado(resumen.getAlteraciones(), Sensor.HUMEDAD, humBloquesClase);
        agregarSiAlterado(resumen.getAlteraciones(), Sensor.DB, dbBloquesClase);
        agregarSiAlterado(resumen.getAlteraciones(), Sensor.LUX, luxBloquesClase);
        agregarSiAlterado(resumen.getAlteraciones(), Sensor.ECO2, eco2BloquesClase);
        agregarSiAlterado(resumen.getAlteraciones(), Sensor.TVOC, tvocBloquesClase);

        contarAlteracionesPorLectura(delDia, resumen.getAlteracionesJornada());

        return resumen;
    }

    private ResumenDiaDto resumenDiaNoHabil(LocalDate fecha) {
        ResumenDiaDto vacio = new ResumenDiaDto();
        vacio.setFecha(fecha);
        vacio.setDiaHabil(false);
        vacio.setMensaje("La fecha no es día hábil escolar (lunes a viernes).");
        vacio.setEstadisticas(List.of());
        return vacio;
    }

    private List<EstadisticaSensorDto> calcularEstadisticas(List<Medicion> lecturas) {
        if (lecturas.isEmpty()) {
            return List.of();
        }
        return List.of(
                estadisticaSensor(Sensor.TEMPERATURA, lecturas, 1),
                estadisticaSensor(Sensor.HUMEDAD, lecturas, 1),
                estadisticaSensor(Sensor.DB, lecturas, 1),
                estadisticaSensor(Sensor.LUX, lecturas, 0),
                estadisticaSensor(Sensor.ECO2, lecturas, 0),
                estadisticaSensor(Sensor.TVOC, lecturas, 0)
        );
    }

    private EstadisticaSensorDto estadisticaSensor(Sensor sensor, List<Medicion> lecturas, int decimales) {
        List<Double> valores = lecturas.stream()
                .map(m -> RangosAmbientalesUtil.obtenerValor(m, sensor))
                .filter(v -> v != null)
                .toList();

        EstadisticaSensorDto dto = new EstadisticaSensorDto();
        dto.setSensor(sensor.getEtiqueta());
        dto.setUnidad(sensor.getUnidad());

        if (valores.isEmpty()) {
            return dto;
        }

        double min = valores.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
        double max = valores.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        double avg = valores.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);

        dto.setMinimo(redondear(min, decimales));
        dto.setMaximo(redondear(max, decimales));
        dto.setPromedio(redondear(avg, decimales));
        return dto;
    }

    private double redondear(double valor, int decimales) {
        if (Double.isNaN(valor)) {
            return Double.NaN;
        }
        double factor = Math.pow(10, decimales);
        return Math.round(valor * factor) / factor;
    }

    private void contarAlteracionesPorLectura(List<Medicion> lecturas, List<AlteracionSensorDto> destino) {
        agregarSiAlterado(destino, Sensor.TEMPERATURA, contarLecturasAlteradas(lecturas, Sensor.TEMPERATURA));
        agregarSiAlterado(destino, Sensor.HUMEDAD, contarLecturasAlteradas(lecturas, Sensor.HUMEDAD));
        agregarSiAlterado(destino, Sensor.DB, contarLecturasAlteradas(lecturas, Sensor.DB));
        agregarSiAlterado(destino, Sensor.LUX, contarLecturasAlteradas(lecturas, Sensor.LUX));
        agregarSiAlterado(destino, Sensor.ECO2, contarLecturasAlteradas(lecturas, Sensor.ECO2));
        agregarSiAlterado(destino, Sensor.TVOC, contarLecturasAlteradas(lecturas, Sensor.TVOC));
    }

    private int contarLecturasAlteradas(List<Medicion> lecturas, Sensor sensor) {
        return (int) lecturas.stream()
                .filter(m -> RangosAmbientalesUtil.estaAlterado(sensor, RangosAmbientalesUtil.obtenerValor(m, sensor)))
                .count();
    }

    private LecturaInformeDto toLecturaDto(Medicion m) {
        LecturaInformeDto dto = new LecturaInformeDto();
        dto.setHora(m.getFechaHora() != null ? HORA_LECTURA.format(m.getFechaHora()) : "—");
        dto.setTemperatura(fmt1(m.getTemperatura()));
        dto.setHumedad(fmt1(m.getHumedad()));
        dto.setDb(fmt1(m.getDb()));
        dto.setLux(fmt0(m.getLux()));
        dto.setEco2(fmt0(m.getEco2()));
        dto.setTvoc(fmt0(m.getTvoc()));
        dto.setTemperaturaAlterada(RangosAmbientalesUtil.estaAlterado(Sensor.TEMPERATURA, m.getTemperatura()));
        dto.setHumedadAlterada(RangosAmbientalesUtil.estaAlterado(Sensor.HUMEDAD, m.getHumedad()));
        dto.setDbAlterada(RangosAmbientalesUtil.estaAlterado(Sensor.DB, m.getDb()));
        dto.setLuxAlterada(RangosAmbientalesUtil.estaAlterado(Sensor.LUX, m.getLux()));
        dto.setEco2Alterada(RangosAmbientalesUtil.estaAlterado(Sensor.ECO2, m.getEco2()));
        dto.setTvocAlterada(RangosAmbientalesUtil.estaAlterado(Sensor.TVOC, m.getTvoc()));
        return dto;
    }

    private SalaInformeDto obtenerSalaInforme() {
        return salaRepository.findAll().stream()
                .findFirst()
                .map(this::toSalaInformeDto)
                .orElse(null);
    }

    private SalaInformeDto toSalaInformeDto(Sala sala) {
        SalaInformeDto dto = new SalaInformeDto();
        dto.setNombre(sala.getNombre());
        dto.setM2(sala.getM2());
        dto.setCantidadEstudiantes(sala.getCantidadEstudiantes());
        dto.setCantidadVentanas(sala.getCantidadVentanas());
        dto.setAireAcondicionado(formatearAireAcondicionado(sala.getAireAcondicionado()));
        dto.setTipoDeVentilacion(sala.getTipoDeVentilacion());
        dto.setDescripcionVentilacion(describirVentilacion(sala.getTipoDeVentilacion()));
        dto.setNumeroPiso(sala.getNumeroPiso());
        return dto;
    }

    private String formatearAireAcondicionado(String valor) {
        if (valor == null || valor.isBlank()) {
            return "No especificado";
        }
        String lower = valor.toLowerCase(Locale.ROOT);
        if ("true".equals(lower) || "si".equals(lower) || "sí".equals(lower)) {
            return "Sí";
        }
        if ("false".equals(lower) || "no".equals(lower)) {
            return "No";
        }
        return valor;
    }

    private String describirVentilacion(String tipo) {
        if (tipo == null) {
            return "No especificada";
        }
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "cruzada" -> "Ventilación cruzada";
            case "unilateral" -> "Ventilación unilateral";
            case "ninguna" -> "Sin ventilación";
            default -> tipo;
        };
    }

    private void agregarSiAlterado(List<AlteracionSensorDto> destino, Sensor sensor, int veces) {
        if (veces > 0) {
            AlteracionSensorDto alteracion = new AlteracionSensorDto();
            alteracion.setSensor(sensor.getEtiqueta());
            alteracion.setVecesAlterada(veces);
            destino.add(alteracion);
        }
    }

    private double promedio(List<Medicion> lista, ToDoubleFunction<Medicion> extractor) {
        return lista.stream()
                .mapToDouble(extractor)
                .average()
                .orElse(Double.NaN);
    }

    private String fmt1(Double v) {
        return v == null ? "—" : String.format(Locale.US, "%.1f", v);
    }

    private String fmt0(Double v) {
        return v == null ? "—" : String.format(Locale.US, "%.0f", v);
    }
}
