package cl.duoc.monitoriza.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ChatIntentClassifierTest {

    @Test
    void clasificaEducativo() {
        assertEquals(ChatIntent.EDUCATIVO, ChatIntentClassifier.clasificar(
                "¿Qué consecuencias tiene el CO2 por encima de 800 ppm? Responde en 2 oraciones."));
    }

    @Test
    void clasificaDiaActual() {
        assertEquals(ChatIntent.DIA_ACTUAL, ChatIntentClassifier.clasificar(
                "Según las mediciones de hoy, ¿qué sensores están alterados?"));
    }

    @Test
    void clasificaHistorico() {
        assertEquals(ChatIntent.HISTORICO, ChatIntentClassifier.clasificar(
                "En los informes del mes, ¿qué sensor tuvo más alteraciones?"));
    }

    @Test
    void clasificaMixto() {
        assertEquals(ChatIntent.MIXTO, ChatIntentClassifier.clasificar(
                "Compara hoy con los informes del mes"));
    }
}
