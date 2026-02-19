package com.sudhird.that_interviewer.service;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class EmbeddingClient {

    private final WebClient webClient;
    private final Duration timeout;

    public EmbeddingClient(
            WebClient.Builder builder,
            @Value("${embedding.service.url}") String baseUrl,
            @Value("${embedding.service.timeout-seconds}") int timeoutSeconds
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    /**
     * Calls POST /similarity on the embedding service.
     *
     * @return cosine similarity 0.0–1.0, or empty if the service is unavailable
     */
    public Optional<Double> getSimilarity(String userAnswer, String idealAnswer) {
        try {
            SimilarityRequest body = new SimilarityRequest(userAnswer, idealAnswer);

            SimilarityResponse response = webClient.post()
                    .uri("/similarity")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(SimilarityResponse.class)
                    .timeout(timeout)
                    .onErrorResume(e -> {
                        log.warn("Embedding service unavailable: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response == null) return Optional.empty();

            log.info("Embedding similarity: {}", response.similarity());
            return Optional.of(response.similarity());

        } catch (Exception e) {
            log.warn("Failed to get similarity from embedding service: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // ── Internal DTOs (only used by this client) ──────────────────────────────

    private record SimilarityRequest(
            String user_answer,
            String ideal_answer
    ) {}

    private record SimilarityResponse(
            double similarity,
            String user_answer,
            String ideal_answer
    ) {}
}
