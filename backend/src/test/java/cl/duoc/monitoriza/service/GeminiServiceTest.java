package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cl.duoc.monitoriza.dto.AlteracionSensorDto;
import cl.duoc.monitoriza.dto.GeminiInformeResponseDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;

class GeminiServiceTest {

    private ObjectMapper objectMapper;
    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        geminiService = new GeminiService(
                RestClient.builder().baseUrl("https://generativelanguage.googleapis.com").build(),
                objectMapper,
                "test-key",
                "gemini-2.5-flash",
                "",
                512
        );
    }

    @Test
    void parseResponseTextJsonValido() {
        String json = """
                {
                  "seccionAnalisis": [
                    { "sensor": "Temperatura", "vecesAlterada": 2, "consecuencia": "Reduce la concentracion." }
                  ],
                  "solucionCortoPlazo": "Ventilar el aula",
                  "solucionLargoPlazo": "Instalar aire acondicionado"
                }
                """;

        GeminiInformeResponseDto result = geminiService.parseResponseText(json);

        assertEquals(1, result.getSeccionAnalisis().size());
        assertEquals("Ventilar el aula", result.getSolucionCortoPlazo());
        assertEquals("Instalar aire acondicionado", result.getSolucionLargoPlazo());
    }

    @Test
    void parseResponseTextLimpiaMarkdown() {
        String markdown = """
                ```json
                {"seccionAnalisis":[],"solucionCortoPlazo":"A","solucionLargoPlazo":"B"}
                ```
                """;

        GeminiInformeResponseDto result = geminiService.parseResponseText(markdown);

        assertNotNull(result.getSeccionAnalisis());
        assertEquals("A", result.getSolucionCortoPlazo());
    }

    @Test
    void enrichWithResumenAlteracionesUsaConteosDelResumen() {
        ResumenDiaDto resumen = new ResumenDiaDto();
        AlteracionSensorDto alt = new AlteracionSensorDto();
        alt.setSensor("Temperatura");
        alt.setVecesAlterada(3);
        resumen.setAlteracionesJornada(List.of(alt));

        GeminiInformeResponseDto respuesta = new GeminiInformeResponseDto();
        AlteracionSensorDto geminiAlt = new AlteracionSensorDto();
        geminiAlt.setSensor("Temperatura");
        geminiAlt.setVecesAlterada(99);
        geminiAlt.setConsecuencia("Impacto en el aprendizaje.");
        respuesta.setSeccionAnalisis(new ArrayList<>(List.of(geminiAlt)));

        geminiService.enrichWithResumenAlteraciones(resumen, respuesta);

        assertEquals(1, respuesta.getSeccionAnalisis().size());
        assertEquals(3, respuesta.getSeccionAnalisis().get(0).getVecesAlterada());
        assertEquals("Impacto en el aprendizaje.", respuesta.getSeccionAnalisis().get(0).getConsecuencia());
    }

    @Test
    void enrichSinAlteracionesDeixaSeccionVacia() {
        ResumenDiaDto resumen = new ResumenDiaDto();
        resumen.setAlteracionesJornada(new ArrayList<>());

        GeminiInformeResponseDto respuesta = new GeminiInformeResponseDto();
        AlteracionSensorDto geminiAlt = new AlteracionSensorDto();
        geminiAlt.setSensor("Temperatura");
        respuesta.setSeccionAnalisis(new ArrayList<>(List.of(geminiAlt)));

        geminiService.enrichWithResumenAlteraciones(resumen, respuesta);

        assertTrue(respuesta.getSeccionAnalisis().isEmpty());
    }

    @Test
    void apiKeyVaciaLanzaError() {
        GeminiService sinKey = new GeminiService(
                RestClient.builder().baseUrl("https://generativelanguage.googleapis.com").build(),
                objectMapper,
                "",
                "gemini-2.5-flash",
                "",
                512
        );

        assertTrue(!sinKey.tieneApiKeyConfigurada());

        ResumenDiaDto resumen = new ResumenDiaDto();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> sinKey.generarAnalisis(resumen)
        );
        assertTrue(ex.getMessage().contains("GEMINI_API_KEY"));
        assertTrue(GeminiService.esErrorDeAutenticacion(ex));
    }

    @Test
    void tieneApiKeyConfiguradaConClaveValida() {
        assertTrue(geminiService.tieneApiKeyConfigurada());
    }

    @Test
    void esErrorDeAutenticacionDetectaRespuesta403() {
        IllegalStateException ex = new IllegalStateException(
                "Error llamando a Gemini (403): API key not valid. Please pass a valid API key.");
        assertTrue(GeminiService.esErrorDeAutenticacion(ex));
    }

    @Test
    void construirPromptChatSinContexto() {
        String prompt = geminiService.construirPromptChat("", "¿Qué es CO2?");
        assertTrue(prompt.contains("Pregunta del usuario:"));
        assertTrue(prompt.contains("¿Qué es CO2?"));
    }

    @Test
    void construirPromptChatConContexto() {
        String prompt = geminiService.construirPromptChat("Alteraciones: CO2=2", "¿Hay problemas hoy?");
        assertTrue(prompt.contains("Alteraciones: CO2=2"));
        assertTrue(prompt.contains("¿Hay problemas hoy?"));
    }
}
