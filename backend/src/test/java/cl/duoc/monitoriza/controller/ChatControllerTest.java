package cl.duoc.monitoriza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.duoc.monitoriza.dto.ChatRequestDto;
import cl.duoc.monitoriza.dto.ChatResponseDto;
import cl.duoc.monitoriza.service.ChatService;
import cl.duoc.monitoriza.util.ChatIntent;

class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void postChatOk() {
        when(chatService.responder(any())).thenReturn(
                new ChatResponseDto("Respuesta de prueba", ChatIntent.EDUCATIVO));

        ChatRequestDto request = new ChatRequestDto();
        request.setMensaje("¿Qué es CO2?");

        ResponseEntity<?> response = chatController.chat(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ChatResponseDto body = (ChatResponseDto) response.getBody();
        assertEquals("Respuesta de prueba", body.getRespuesta());
        assertEquals(ChatIntent.EDUCATIVO, body.getIntent());
    }

    @Test
    void postChatErrorValidacion() {
        when(chatService.responder(any())).thenThrow(new IllegalArgumentException("El mensaje no puede estar vacío."));

        ChatRequestDto request = new ChatRequestDto();
        request.setMensaje("");

        ResponseEntity<?> response = chatController.chat(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void postChatLimiteExcedido() {
        when(chatService.responder(any())).thenThrow(
                new IllegalStateException("Demasiados mensajes seguidos. Espera un minuto e intenta de nuevo."));

        ChatRequestDto request = new ChatRequestDto();
        request.setMensaje("¿Qué es CO2?");

        ResponseEntity<?> response = chatController.chat(request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
    }
}
