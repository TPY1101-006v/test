package cl.duoc.monitoriza.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.duoc.monitoriza.dto.ChatResponseDto;
import cl.duoc.monitoriza.util.ChatIntent;
import cl.duoc.monitoriza.util.ChatIntentClassifier;

@Service
public class ChatService {

    private final GeminiService geminiService;
    private final ChatContextBuilder chatContextBuilder;
    private final ChatRateLimiter chatRateLimiter;
    private final boolean chatEnabled;
    private final int maxMensajesPorDia;
    private final int maxMensajesPorMinuto;

    public ChatService(
            GeminiService geminiService,
            ChatContextBuilder chatContextBuilder,
            ChatRateLimiter chatRateLimiter,
            @Value("${app.chat.enabled:true}") boolean chatEnabled,
            @Value("${app.chat.max-mensajes-por-dia:30}") int maxMensajesPorDia,
            @Value("${app.chat.max-mensajes-por-minuto:3}") int maxMensajesPorMinuto) {
        this.geminiService = geminiService;
        this.chatContextBuilder = chatContextBuilder;
        this.chatRateLimiter = chatRateLimiter;
        this.chatEnabled = chatEnabled;
        this.maxMensajesPorDia = maxMensajesPorDia;
        this.maxMensajesPorMinuto = maxMensajesPorMinuto;
    }

    public ChatResponseDto responder(String mensaje) {
        if (!chatEnabled) {
            throw new IllegalStateException("El chat con IA está desactivado.");
        }
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }

        chatRateLimiter.verificarLimites(maxMensajesPorDia, maxMensajesPorMinuto);

        ChatIntent intent = ChatIntentClassifier.clasificar(mensaje);
        String contexto = construirContexto(intent);

        String respuesta = geminiService.responderChat(contexto, mensaje.trim());
        return new ChatResponseDto(respuesta, intent);
    }

    private String construirContexto(ChatIntent intent) {
        LocalDate hoy = LocalDate.now();

        return switch (intent) {
            case EDUCATIVO -> "";
            case DIA_ACTUAL -> chatContextBuilder.construirContextoDia(hoy);
            case HISTORICO -> chatContextBuilder.construirContextoPatrones();
            case MIXTO -> chatContextBuilder.construirContextoDia(hoy) + "\n"
                    + chatContextBuilder.construirContextoPatrones();
        };
    }
}
