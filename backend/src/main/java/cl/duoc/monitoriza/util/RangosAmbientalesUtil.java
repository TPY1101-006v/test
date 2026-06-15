package cl.duoc.monitoriza.util;

import cl.duoc.monitoriza.model.Medicion;

/**
 * Rangos ideales alineados con {@code frontend/src/utils/constants.js} (SENSORS min/max).
 */
public final class RangosAmbientalesUtil {

    public static final double TEMP_MIN = 23.0;
    public static final double TEMP_MAX = 26.0;
    public static final double HUMEDAD_MIN = 35.0;
    public static final double HUMEDAD_MAX = 50.0;
    public static final double DB_MIN = 30.0;
    public static final double DB_MAX = 50.0;
    public static final double LUX_MIN = 300.0;
    public static final double LUX_MAX = 500.0;
    public static final double ECO2_MIN = 400.0;
    public static final double ECO2_MAX = 800.0;
    public static final double TVOC_MIN = 0.0;
    public static final double TVOC_MAX = 500.0;

    public enum Sensor {
        TEMPERATURA("Temperatura", "°C", TEMP_MIN, TEMP_MAX),
        HUMEDAD("Humedad", "%", HUMEDAD_MIN, HUMEDAD_MAX),
        DB("Decibeles", "dB", DB_MIN, DB_MAX),
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

    public static boolean dbAlterado(Double v) {
        return estaFueraDeRango(v, DB_MIN, DB_MAX);
    }

    public static boolean luxAlterada(Double v) {
        return estaFueraDeRango(v, LUX_MIN, LUX_MAX);
    }

    public static boolean eco2Alterado(Double v) {
        return estaFueraDeRango(v, ECO2_MIN, ECO2_MAX);
    }

    public static boolean tvocAlterado(Double v) {
        return estaFueraDeRango(v, TVOC_MIN, TVOC_MAX);
    }

    public static boolean estaAlterado(Sensor sensor, Double valor) {
        return estaFueraDeRango(valor, sensor.getMin(), sensor.getMax());
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
