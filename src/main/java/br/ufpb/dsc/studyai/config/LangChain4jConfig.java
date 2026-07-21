package br.ufpb.dsc.studyai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChain4jConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(IAProperties props) {
        if (props.isDemo()) {
            // Mock simples para o Spring conseguir injetar no @AiService sem falhar.
            // O FlashcardService intercepta chamadas no modo demo antes de chegarem aqui.
            return new ChatLanguageModel() {
                @Override
                public dev.langchain4j.model.output.Response<dev.langchain4j.data.message.AiMessage> generate(java.util.List<dev.langchain4j.data.message.ChatMessage> messages) {
                    return dev.langchain4j.model.output.Response.from(dev.langchain4j.data.message.AiMessage.from("demo"));
                }
            };
        }

        String provedor = props.getProvedor() != null ? props.getProvedor().toLowerCase() : "";
        if ("gemini".equals(provedor)) {
            return GoogleAiGeminiChatModel.builder()
                    .apiKey(props.getApiKey())
                    .modelName(props.getModelo())
                    .timeout(Duration.ofSeconds(props.getTimeoutSegundos()))
                    .build();
        } else {
            // Fallback para Anthropic, que é o padrão
            return AnthropicChatModel.builder()
                    .apiKey(props.getApiKey())
                    .modelName(props.getModelo())
                    .timeout(Duration.ofSeconds(props.getTimeoutSegundos()))
                    .build();
        }
    }
}
