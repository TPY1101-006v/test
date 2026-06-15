package cl.duoc.monitoriza.dto;

import cl.duoc.monitoriza.util.ChatIntent;

public class ChatResponseDto {

    private String respuesta;
    private ChatIntent intent;

    public ChatResponseDto() {}

    public ChatResponseDto(String respuesta, ChatIntent intent) {
        this.respuesta = respuesta;
        this.intent = intent;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public ChatIntent getIntent() {
        return intent;
    }

    public void setIntent(ChatIntent intent) {
        this.intent = intent;
    }
}
