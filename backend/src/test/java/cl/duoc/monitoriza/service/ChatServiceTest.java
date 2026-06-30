package cl.duoc.monitoriza.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cl.duoc.monitoriza.dto.ChatResponseDto;
import cl.duoc.monitoriza.util.ChatIntent;

class ChatServiceTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private ChatContextBuilder chatContextBuilder;

    private ChatRateLimiter chatRateLimiter;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatRateLimiter = new ChatRateLimiter();
        chatService = new ChatService(
                geminiService, chatContextBuilder, chatRateLimiter, true, 30, 3);
    }

    @Test
    void responderEducativoSinContexto() {
        when(geminiService.responderChat(eq(""), any())).thenReturn("Respuesta educativa.");

        ChatResponseDto result = chatService.responder("¿Qué es TVOC?");

        assertEquals(ChatIntent.EDUCATIVO, result.getIntent());
        assertEquals("Respuesta educativa.", result.getRespuesta());
        verify(geminiService).responderChat("", "¿Qué es TVOC?");
    }

    @Test
    void responderDiaActualConContextoCompacto() {
        when(chatContextBuilder.construirContextoDia(any())).thenReturn("Alteraciones acumuladas: CO2=3.");
        when(geminiService.responderChat(any(), any())).thenReturn("CO2");

        ChatResponseDto result = chatService.responder("¿Qué sensores están alterados hoy?");

        assertEquals(ChatIntent.DIA_ACTUAL, result.getIntent());
        verify(geminiService).responderChat("Alteraciones acumuladas: CO2=3.", "¿Qué sensores están alterados hoy?");
    }

    @Test
    void responderHistoricoConPatrones() {
        when(chatContextBuilder.construirContextoPatrones()).thenReturn("Patrones: CO2=120.");
        when(geminiService.responderChat(any(), any())).thenReturn("Dióxido de Carbono, 120");

        ChatResponseDto result = chatService.responder("¿Qué sensor falla más en los informes del mes?");

        assertEquals(ChatIntent.HISTORICO, result.getIntent());
        verify(geminiService).responderChat("Patrones: CO2=120.", "¿Qué sensor falla más en los informes del mes?");
    }

    @Test
    void mensajeVacioLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> chatService.responder("   "));
    }

    @Test
    void chatDesactivadoLanzaError() {
        ChatService desactivado = new ChatService(
                geminiService, chatContextBuilder, chatRateLimiter, false, 30, 3);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> desactivado.responder("¿Qué es CO2?"));
        assertTrue(ex.getMessage().contains("desactivado"));
    }

    @Test
    void limiteDiarioAlcanzado() {
        ChatService limitado = new ChatService(
                geminiService, chatContextBuilder, chatRateLimiter, true, 1, 3);
        when(geminiService.responderChat(any(), any())).thenReturn("ok");

        limitado.responder("¿Qué es CO2?");
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> limitado.responder("¿Qué es humedad?"));
        assertTrue(ex.getMessage().contains("Límite diario"));
    }
}
