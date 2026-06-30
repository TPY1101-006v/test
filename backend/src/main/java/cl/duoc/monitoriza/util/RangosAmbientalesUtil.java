package cl.duoc.monitoriza.util;

import cl.duoc.monitoriza.model.Medicion;

/**
 * Rangos ideales alineados con {@code frontend/src/utils/constants.js} (SENSORS min/max).
 */
public final class RangosAmbientalesUtil {

    public static final double TEMP_MIN = 20.0;
    public static final double TEMP_MAX = 22.0;
    public static final double HUMEDAD_MIN = 40.0;
    public static final double HUMEDAD_MAX = 60.0;
    public static final double DB_MIN = 35.0;
    public static final double DB_MAX = 45.0;
    public static final double LUX_MIN = 300.0;
    public static final double LUX_MAX = 500.0;
    public static final double ECO2_MIN = 400.0;
    public static final double ECO2_MAX = 800.0;
    public static final double TVOC_MIN = 0.0;
    public static final double TVOC_MAX = 220.0;

    public enum Sensor {
        TEMPERATURA("Temperatura", "°C", TEMP_MIN, TEMP_MAX),
        HUMEDAD("Humedad", "%", HUMEDAD_MIN, HUMEDAD_MAX),
        DB("Decibeles", "dBA", DB_MIN, DB_MAX),
        LUX("Iluminación", "lx", LUX_MIN, LUX_MAX),
        ECO2("Dióxido de Carbono", "ppm", ECO2_MIN, ECO2_MAX),
        TVOC("Compuestos Orgánicos Volátiles", "ppb", TVOC_MIN, TVOC_MAX);

        private final String etiqueta;
        private final String unidad;
        private final double min;
        private final double max;

        Sensor(String etiqueta, String unidad, double min, double max) {
            this.etiqueta = etiqueta;
            this.unidad = unidad;
            this.min = min;
            this.max = max;
        }

        public String getEtiqueta() { return etiqueta; }
        public String getUnidad() { return unidad; }
        public double getMin() { return min; }
        public double getMax() { return max; }
    }

    private RangosAmbientalesUtil() {}

    public static boolean temperaturaAlterada(Double v) {
        return estaFueraDeRango(v, TEMP_MIN, TEMP_MAX);
    }

    public static boolean humedadAlterada(Double v) {
        return estaFueraDeRango(v, HUMEDAD_MIN, HUMEDAD_MAX);
    }

    /** Por debajo de 35 dBA no hay consecuencias negativas; solo alerta si supera el máximo. */
    public static boolean dbAlterado(Double v) {
        return v != null && v > DB_MAX;
    }

    public static boolean luxAlterada(Double v) {
        return estaFueraDeRango(v, LUX_MIN, LUX_MAX);
    }

    /** Por debajo de 400 ppm el aire es más limpio; solo alerta si supera el máximo. */
    public static boolean eco2Alterado(Double v) {
        return v != null && v > ECO2_MAX;
    }

    /** Por debajo del límite no hay impacto negativo; solo alerta si supera el máximo. */
    public static boolean tvocAlterado(Double v) {
        return v != null && v > TVOC_MAX;
    }

    public static boolean estaAlterado(Sensor sensor, Double valor) {
        return switch (sensor) {
            case TEMPERATURA -> temperaturaAlterada(valor);
            case HUMEDAD     -> humedadAlterada(valor);
            case DB          -> dbAlterado(valor);
            case LUX         -> luxAlterada(valor);
            case ECO2        -> eco2Alterado(valor);
            case TVOC        -> tvocAlterado(valor);
        };
    }

    public static boolean estaFueraDeRango(Double valor, double min, double max) {
        return valor != null && (valor < min || valor > max);
    }

    public static Double obtenerValor(Medicion m, Sensor sensor) {
        return switch (sensor) {
            case TEMPERATURA -> m.getTemperatura();
            case HUMEDAD     -> m.getHumedad();
            case DB          -> m.getDb();
            case LUX         -> m.getLux();
            case ECO2        -> m.getEco2();
            case TVOC        -> m.getTvoc();
        };
    }
}
