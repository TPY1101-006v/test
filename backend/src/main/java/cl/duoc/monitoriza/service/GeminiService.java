package cl.duoc.monitoriza.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.monitoriza.dto.AlteracionSensorDto;
import cl.duoc.monitoriza.dto.GeminiInformeResponseDto;
import cl.duoc.monitoriza.dto.ResumenDiaDto;
import cl.duoc.monitoriza.dto.SolucionDetalleDto;
import cl.duoc.monitoriza.util.RangosAmbientalesUtil;

@Service
public class GeminiService {

    private static final String SYSTEM_PROMPT = """
            Eres un especialista en analisis ambiental de salas de la empresa Monitoriza.
            Tu unico objetivo es analizar lecturas de sensores ambientales de aula.

            Reglas obligatorias:
            - Basate unicamente en evidencia y criterios de estudios cientificos sobre confort ambiental, calidad de aire interior y aprendizaje.
            - Evalua estrictamente los datos entregados, sin inventar informacion.
            - Si algun dato viene nulo o faltante, indicalo y limita el analisis a lo disponible.
            - Esta completamente prohibido responder temas que no correspondan a la app y sus sensores:
              politica, generacion de codigo, arquitectura de software, arte u otros temas ajenos.
            - Si la solicitud no corresponde al analisis ambiental de sensores del aula, responde exactamente:
              "Solo puedo analizar lecturas de sensores ambientales del aula."

            Horarios escolares (MUY IMPORTANTE):
            - Dias habiles: Lunes a Viernes unicamente.
            - Horario Antes de iniciar clases: 7:30-7:59
            - Horario despues de clases: 15:30 - 16:30
            - Bloques de clase: 08:00-09:29, 09:50-11:19, 11:30-12:59 y 14:00-15:29.
            - Recreos (NO son clase): 09:30-09:49 y 11:20-11:29.
            - Almuerzo (NO es clase): 13:00-13:59.
            - Las lecturas incluyen mediciones individuales por bloque horario de 7:30 a 16:30.
            - Debes considerar SIEMPRE la informacion de la sala (m2, estudiantes, ventanas, ventilacion, aire acondicionado, piso).

            Rangos de referencia para evaluar ideal/no ideal (alineados con la app):
            - Temperatura ideal: %.0f-%.0f C
            - Humedad ideal: %.0f-%.0f %%
            - Luz ideal: %.0f-%.0f lux
            - Ruido ideal: %.0f-%.0f dB
            - CO2 ideal: %.0f-%.0f ppm
            - TVOC ideal: %.0f-%.0f ppb
            """.formatted(
            RangosAmbientalesUtil.TEMP_MIN, RangosAmbientalesUtil.TEMP_MAX,
            RangosAmbientalesUtil.HUMEDAD_MIN, RangosAmbientalesUtil.HUMEDAD_MAX,
            RangosAmbientalesUtil.LUX_MIN, RangosAmbientalesUtil.LUX_MAX,
            RangosAmbientalesUtil.DB_MIN, RangosAmbientalesUtil.DB_MAX,
            RangosAmbientalesUtil.ECO2_MIN, RangosAmbientalesUtil.ECO2_MAX,
            RangosAmbientalesUtil.TVOC_MIN, RangosAmbientalesUtil.TVOC_MAX);

    private static final String CHAT_SYSTEM_PROMPT = """
            Eres un educador ambiental de Monitoriza, especialista en confort de aulas escolares.
            Ayudas a docentes a entender sensores ambientales (temperatura, humedad, ruido, luz, CO2, TVOC).

            Reglas:
            - Responde en español claro, tono pedagogico y breve (maximo 3 oraciones salvo que pidan detalle).
            - Basate en los datos del contexto cuando se entreguen; no inventes mediciones.
            - Si no hay datos en el contexto, responde con conocimiento general sobre sensores y rangos ideales.
            - Solo temas de sensores ambientales del aula. Si la pregunta es ajena, responde exactamente:
              "Solo puedo analizar lecturas de sensores ambientales del aula."

            Rangos ideales:
            - Temperatura: %.0f-%.0f C
            - Humedad: %.0f-%.0f %%
            - Ruido: %.0f-%.0f dB
            - Luz: %.0f-%.0f lux
            - CO2: %.0f-%.0f ppm
            - TVOC: %.0f-%.0f ppb
            """.formatted(
            RangosAmbientalesUtil.TEMP_MIN, RangosAmbientalesUtil.TEMP_MAX,
            RangosAmbientalesUtil.HUMEDAD_MIN, RangosAmbientalesUtil.HUMEDAD_MAX,
            RangosAmbientalesUtil.DB_MIN, RangosAmbientalesUtil.DB_MAX,
            RangosAmbientalesUtil.LUX_MIN, RangosAmbientalesUtil.LUX_MAX,
            RangosAmbientalesUtil.ECO2_MIN, RangosAmbientalesUtil.ECO2_MAX,
            RangosAmbientalesUtil.TVOC_MIN, RangosAmbientalesUtil.TVOC_MAX);

    private static final String USER_PROMPT_TEMPLATE = """
            Resumen estructurado del dia (JSON con sala, bloques, lecturas individuales y promedios):
            %s

            Alteraciones de la jornada completa 7:30-16:30 (conteo por LECTURA individual, usa estos numeros exactos):
            %s

            Total de lecturas en jornada: %d

            Genera SOLO un JSON valido con esta estructura exacta:
            {
              "contextoSala": "Parrafo que mencione m2, estudiantes, ventanas, ventilacion, piso y aire acondicionado...",
              "seccionAnalisis": [
                {
                  "sensor": "Temperatura",
                  "vecesAlterada": 12,
                  "consecuencia": "Analisis detallado de 3-5 oraciones sobre impacto en estudiantes, mencionando la sala si aplica."
                }
              ],
              "solucionesCortoPlazo": [
                {
                  "titulo": "Abrir ventanas",
                  "indicadores": ["Dióxido de Carbono", "Temperatura"],
                  "explicacion": "Explicacion detallada de 2-4 oraciones."
                }
              ],
              "solucionesLargoPlazo": [
                {
                  "titulo": "Instalar aire acondicionado",
                  "indicadores": ["Temperatura", "Humedad"],
                  "explicacion": "Explicacion detallada de 2-4 oraciones considerando la sala."
                }
              ]
            }

            Reglas de salida:
            - contextoSala: obligatorio si hay datos de sala; menciona densidad (estudiantes/m2) cuando sea posible.
            - seccionAnalisis: SOLO sensores en alteracionesJornada; vecesAlterada EXACTO al conteo entregado.
            - consecuencia: analisis detallado (minimo 3 oraciones), basado en TODAS las lecturas alteradas de la jornada.
            - solucionesCortoPlazo y solucionesLargoPlazo: minimo 2 items cada uno si hay alteraciones; cada item con titulo, indicadores[] y explicacion detallada.
            - Las soluciones deben considerar ventanas, ventilacion y si hay o no aire acondicionado.
            - Si no hay alteraciones, seccionAnalisis=[], soluciones con recomendaciones preventivas generales.
            - Responde SOLO el JSON, sin markdown.
            """;

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String chatModel;
    private final int chatMaxOutputTokens;

    public GeminiService(
            RestClient geminiRestClient,
            ObjectMapper objectMapper,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.chat.model:}") String chatModel,
            @Value("${app.chat.max-output-tokens:512}") int chatMaxOutputTokens) {
        this.geminiRestClient = geminiRestClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.chatModel = chatModel == null || chatModel.isBlank() ? model : chatModel;
        this.chatMaxOutputTokens = chatMaxOutputTokens;
    }

    public String responderChat(String contextoDatos, String mensajeUsuario) {
        validarApiKey();

        String userPrompt = construirPromptChat(contextoDatos, mensajeUsuario);
        return llamarGeminiTexto(chatModel, CHAT_SYSTEM_PROMPT, userPrompt, false, chatMaxOutputTokens);
    }

    String construirPromptChat(String contextoDatos, String mensajeUsuario) {
        if (contextoDatos == null || contextoDatos.isBlank()) {
            return "Pregunta del usuario:\n" + mensajeUsuario;
        }
        return """
                Contexto de datos (usa solo esto como evidencia):
                %s

                Pregunta del usuario:
                %s
                """.formatted(contextoDatos.trim(), mensajeUsuario);
    }

    public GeminiInformeResponseDto generarAnalisis(ResumenDiaDto resumen) {
        validarApiKey();

        try {
            String resumenJson = objectMapper.writeValueAsString(resumen);
            String alteracionesJson = objectMapper.writeValueAsString(resumen.getAlteracionesJornada());
            String userPrompt = USER_PROMPT_TEMPLATE.formatted(
                    resumenJson, alteracionesJson, resumen.getTotalLecturasJornada());
            String responseText = llamarGeminiInforme(userPrompt);
            GeminiInformeResponseDto respuesta = parseResponseText(responseText);
            enrichWithResumenAlteraciones(resumen, respuesta);
            normalizarSoluciones(respuesta);
            return respuesta;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error procesando JSON para Gemini", e);
        }
    }

    void enrichWithResumenAlteraciones(ResumenDiaDto resumen, GeminiInformeResponseDto respuesta) {
        List<AlteracionSensorDto> alteraciones = resumen.getAlteracionesJornada();
        if (alteraciones == null || alteraciones.isEmpty()) {
            respuesta.setSeccionAnalisis(new ArrayList<>());
            return;
        }

        Map<String, String> consecuenciasPorSensor = new HashMap<>();
        if (respuesta.getSeccionAnalisis() != null) {
            for (AlteracionSensorDto item : respuesta.getSeccionAnalisis()) {
                if (item.getSensor() != null && item.getConsecuencia() != null) {
                    consecuenciasPorSensor.put(normalizarSensor(item.getSensor()), item.getConsecuencia());
                }
            }
        }

        List<AlteracionSensorDto> merged = new ArrayList<>();
        for (AlteracionSensorDto alt : alteraciones) {
            AlteracionSensorDto item = new AlteracionSensorDto();
            item.setSensor(alt.getSensor());
            item.setVecesAlterada(alt.getVecesAlterada());
            item.setConsecuencia(consecuenciasPorSensor.getOrDefault(
                    normalizarSensor(alt.getSensor()),
                    "Lecturas fuera del rango ideal detectadas durante la jornada escolar (7:30-16:30)."
            ));
            merged.add(item);
        }
        respuesta.setSeccionAnalisis(merged);
    }

    void normalizarSoluciones(GeminiInformeResponseDto respuesta) {
        if (respuesta.getSolucionesCortoPlazo() == null) {
            respuesta.setSolucionesCortoPlazo(new ArrayList<>());
        }
        if (respuesta.getSolucionesLargoPlazo() == null) {
            respuesta.setSolucionesLargoPlazo(new ArrayList<>());
        }
        if (respuesta.getSolucionesCortoPlazo().isEmpty() && respuesta.getSolucionCortoPlazo() != null
                && !respuesta.getSolucionCortoPlazo().isBlank()) {
            SolucionDetalleDto item = new SolucionDetalleDto();
            item.setTitulo("Recomendación general");
            item.setExplicacion(respuesta.getSolucionCortoPlazo());
            respuesta.getSolucionesCortoPlazo().add(item);
        }
        if (respuesta.getSolucionesLargoPlazo().isEmpty() && respuesta.getSolucionLargoPlazo() != null
                && !respuesta.getSolucionLargoPlazo().isBlank()) {
            SolucionDetalleDto item = new SolucionDetalleDto();
            item.setTitulo("Recomendación general");
            item.setExplicacion(respuesta.getSolucionLargoPlazo());
            respuesta.getSolucionesLargoPlazo().add(item);
        }
    }

    GeminiInformeResponseDto parseResponseText(String responseText) {
        try {
            String json = limpiarJson(responseText);
            GeminiInformeResponseDto dto = objectMapper.readValue(json, GeminiInformeResponseDto.class);
            if (dto.getSeccionAnalisis() == null) {
                dto.setSeccionAnalisis(new ArrayList<>());
            }
            if (dto.getSolucionesCortoPlazo() == null) {
                dto.setSolucionesCortoPlazo(new ArrayList<>());
            }
            if (dto.getSolucionesLargoPlazo() == null) {
                dto.setSolucionesLargoPlazo(new ArrayList<>());
            }
            return dto;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Gemini devolvio un JSON invalido: " + responseText, e);
        }
    }

    private String llamarGeminiInforme(String userPrompt) {
        return llamarGeminiTexto(model, SYSTEM_PROMPT, userPrompt, true, null);
    }

    private String llamarGeminiTexto(
            String modelo,
            String systemPrompt,
            String userPrompt,
            boolean jsonMode,
            Integer maxOutputTokens) {
        Map<String, Object> body = construirRequestBody(systemPrompt, userPrompt, jsonMode, maxOutputTokens);
        String uri = "/v1beta/models/" + modelo + ":generateContent?key=" + apiKey;

        try {
            String responseJson = geminiRestClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            if (responseJson == null || responseJson.isBlank()) {
                throw new IllegalStateException("Gemini devolvio respuesta vacia");
            }

            JsonNode response = objectMapper.readTree(responseJson);
            return extraerTextoRespuesta(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Gemini devolvio JSON invalido", e);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "Error llamando a Gemini (" + e.getStatusCode().value() + "): " + e.getResponseBodyAsString(), e);
        }
    }

    private Map<String, Object> construirRequestBody(
            String systemPrompt,
            String userPrompt,
            boolean jsonMode,
            Integer maxOutputTokens) {
        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
        );
        Map<String, Object> content = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userPrompt))
        );

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", jsonMode ? 0.3 : 0.4);
        if (jsonMode) {
            generationConfig.put("responseMimeType", "application/json");
        }
        if (maxOutputTokens != null && maxOutputTokens > 0) {
            generationConfig.put("maxOutputTokens", maxOutputTokens);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("systemInstruction", systemInstruction);
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);
        return body;
    }

    private String extraerTextoRespuesta(JsonNode response) {
        if (response == null) {
            throw new IllegalStateException("Gemini devolvio respuesta vacia");
        }

        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            JsonNode error = response.path("error").path("message");
            if (!error.isMissingNode()) {
                throw new IllegalStateException("Gemini error: " + error.asText());
            }
            throw new IllegalStateException("Gemini no devolvio candidatos");
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new IllegalStateException("Gemini no devolvio contenido de texto");
        }

        String text = parts.get(0).path("text").asText("");
        if (text.isBlank()) {
            throw new IllegalStateException("Gemini devolvio texto vacio");
        }
        return text;
    }

    private String limpiarJson(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    public boolean tieneApiKeyConfigurada() {
        return apiKey != null && !apiKey.isBlank();
    }

    public static boolean esErrorDeAutenticacion(Throwable error) {
        Throwable actual = error;
        while (actual != null) {
            String mensaje = actual.getMessage();
            if (mensaje != null) {
                String normalizado = mensaje.toLowerCase();
                if (normalizado.contains("gemini_api_key")
                        || normalizado.contains("api key")
                        || normalizado.contains("api_key")
                        || normalizado.contains("(401)")
                        || normalizado.contains("(403)")
                        || normalizado.contains("permission_denied")
                        || normalizado.contains("invalid api key")
                        || normalizado.contains("api key not valid")) {
                    return true;
                }
            }
            actual = actual.getCause();
        }
        return false;
    }

    private void validarApiKey() {
        if (!tieneApiKeyConfigurada()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY no configurada. Define la variable de entorno antes de iniciar la app.");
        }
    }

    private String normalizarSensor(String sensor) {
        return sensor == null ? "" : sensor.trim().toLowerCase();
    }
}
