package cl.duoc.monitoriza.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cl.duoc.monitoriza.model.Medicion;

public final class HorarioEscolarUtil {

    private static final LocalTime INICIO_JORNADA = LocalTime.of(7, 30);
    private static final LocalTime FIN_JORNADA    = LocalTime.of(16, 30);

    private HorarioEscolarUtil() {}

    // --- Día hábil ---

    public static boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        return dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
    }

    // --- Rango del día escolar ---

    public static boolean estaEnRangoDiaEscolar(LocalTime hora) {
        return !hora.isBefore(INICIO_JORNADA) && !hora.isAfter(FIN_JORNADA);
    }

    public static LocalDateTime inicioJornada(LocalDate fecha) {
        return fecha.atTime(INICIO_JORNADA);
    }

    public static LocalDateTime finJornada(LocalDate fecha) {
        return fecha.atTime(FIN_JORNADA);
    }

    // --- Bloques ---

    public static boolean esBloqueDeClase(BloqueHorario bloque) {
        return bloque != null && bloque.isBloqueDeClase();
    }

    public static Optional<BloqueHorario> obtenerBloque(LocalTime hora) {
        return Arrays.stream(BloqueHorario.values())
                .filter(b -> b.contiene(hora))
                .findFirst();
    }

    public static List<BloqueHorario> bloquesDeClase() {
        return Arrays.stream(BloqueHorario.values())
                .filter(BloqueHorario::isBloqueDeClase)
                .collect(Collectors.toList());
    }

    // --- Filtros sobre mediciones ---

    /** Mediciones del día entre 07:30 y 16:30 (cualquier bloque) */
    public static List<Medicion> filtrarMedicionesDelDia(List<Medicion> todas, LocalDate fecha) {
        return todas.stream()
                .filter(m -> m.getFechaHora() != null)
                .filter(m -> m.getFechaHora().toLocalDate().equals(fecha))
                .filter(m -> estaEnRangoDiaEscolar(m.getFechaHora().toLocalTime()))
                .collect(Collectors.toList());
    }

    /** Solo lecturas que caen en los 4 bloques de clase */
    public static List<Medicion> filtrarMedicionesBloquesClase(List<Medicion> todas, LocalDate fecha) {
        return filtrarMedicionesDelDia(todas, fecha).stream()
                .filter(m -> obtenerBloque(m.getFechaHora().toLocalTime())
                        .map(BloqueHorario::isBloqueDeClase)
                        .orElse(false))
                .collect(Collectors.toList());
    }

    /** Agrupa mediciones por bloque (incluye recreos, almuerzo, etc.) */
    public static java.util.Map<BloqueHorario, List<Medicion>> agruparPorBloque(
            List<Medicion> medicionesDelDia) {

        return medicionesDelDia.stream()
                .filter(m -> obtenerBloque(m.getFechaHora().toLocalTime()).isPresent())
                .collect(Collectors.groupingBy(
                        m -> obtenerBloque(m.getFechaHora().toLocalTime()).orElseThrow()
                ));
    }
}