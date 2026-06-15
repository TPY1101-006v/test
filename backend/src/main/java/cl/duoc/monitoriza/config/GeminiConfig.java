package cl.duoc.monitoriza.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {

    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com";

    @Bean
    RestClient geminiRestClient() {
        return RestClient.builder()
                .baseUrl(GEMINI_BASE_URL)
                .build();
    }
}
