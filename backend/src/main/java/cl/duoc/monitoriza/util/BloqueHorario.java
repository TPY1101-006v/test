package cl.duoc.monitoriza.util;

import java.time.LocalTime;

public enum BloqueHorario {
    
    ANTES_CLASE(LocalTime.of(7, 30), LocalTime.of(7, 59), false),
    BLOQUE_1(LocalTime.of(8, 0), LocalTime.of(9, 29), true),
    RECREO_1(LocalTime.of(9, 30), LocalTime.of(9, 49), false),
    BLOQUE_2(LocalTime.of(9, 50), LocalTime.of(11, 19), true),
    RECREO_2(LocalTime.of(11, 20), LocalTime.of(11, 29), false),
    BLOQUE_3(LocalTime.of(11, 30), LocalTime.of(12, 59), true),
    ALMUERZO(LocalTime.of(13, 0), LocalTime.of(13, 59), false),
    BLOQUE_4(LocalTime.of(14, 0), LocalTime.of(15, 29), true),
    DESPUES_CLASE(LocalTime.of(15, 30), LocalTime.of(16, 30), false);
    private final LocalTime inicio;
    private final LocalTime fin;
    private final boolean bloqueDeClase;
    BloqueHorario(LocalTime inicio, LocalTime fin, boolean bloqueDeClase) {
        this.inicio = inicio;
        this.fin = fin;
        this.bloqueDeClase = bloqueDeClase;
    }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFin() { return fin; }
    public boolean isBloqueDeClase() { return bloqueDeClase; }
    /** Etiqueta legible para el informe, ej: "08:00-09:29" */
    public String getEtiqueta() {
        return String.format("%02d:%02d-%02d:%02d",
                inicio.getHour(), inicio.getMinute(),
                fin.getHour(), fin.getMinute());
    }

    /** Nombre amigable del bloque para el PDF */
    public String getTituloAmigable() {
        return switch (this) {
            case ANTES_CLASE   -> "Antes de clase";
            case BLOQUE_1      -> "Primera clase";
            case RECREO_1      -> "Recreo 1";
            case BLOQUE_2      -> "Segunda clase";
            case RECREO_2      -> "Recreo 2";
            case BLOQUE_3      -> "Tercera clase";
            case ALMUERZO      -> "Almuerzo";
            case BLOQUE_4      -> "Cuarta clase";
            case DESPUES_CLASE -> "Después de clase";
        };
    }
    /** true si la hora cae dentro del bloque (inclusive en ambos extremos) */
    public boolean contiene(LocalTime hora) {
        return !hora.isBefore(inicio) && !hora.isAfter(fin);
    }

}
