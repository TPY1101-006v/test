package cl.duoc.monitoriza.util;

import java.util.Locale;

public final class ChatIntentClassifier {

    private ChatIntentClassifier() {}

    public static ChatIntent clasificar(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return ChatIntent.EDUCATIVO;
        }

        String m = mensaje.toLowerCase(Locale.ROOT);

        boolean dia = contieneAlguno(m,
                "hoy", "ahora", "actual", "esta jornada", "mediciones de hoy", "del dia", "del día");
        boolean hist = contieneAlguno(m,
                "informe", "informes", "mes", "semana", "historico", "histórico",
                "patron", "patrón", "mas alter", "más alter", "acumulad");
        boolean edu = contieneAlguno(m,
                "que es", "qué es", "consecuencia", "implica", "por que", "por qué",
                "explica", "como afecta", "cómo afecta", "para que sirve", "para qué sirve");

        if (dia && hist) {
            return ChatIntent.MIXTO;
        }
        if (dia) {
            return ChatIntent.DIA_ACTUAL;
        }
        if (hist) {
            return ChatIntent.HISTORICO;
        }
        if (edu) {
            return ChatIntent.EDUCATIVO;
        }

        if (contieneAlguno(m, "co2", "tvoc", "temperatura", "humedad", "ruido", "luz", "ppm", "ppb", "decibel")) {
            return ChatIntent.EDUCATIVO;
        }

        return ChatIntent.DIA_ACTUAL;
    }

    private static boolean contieneAlguno(String texto, String... terminos) {
        for (String termino : terminos) {
            if (texto.contains(termino)) {
                return true;
            }
        }
        return false;
    }
}
