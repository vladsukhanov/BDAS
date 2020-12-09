package ru.spbstu.sukhanov.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.spbstu.sukhanov.handlers.dto.ServeData;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class GatewayHandler {

    private final WebClient webClient;

    public GatewayHandler(final WebClient webClient,
                          @Value("${app.serverUrl}") final String serverUrl) {
        this.webClient = webClient.mutate()
                .baseUrl(serverUrl)
                .build();
    }

    public Mono<ServerResponse> getServerStatus(final ServerRequest request) {
        return webClient.get()
                .uri("/api/data")
                .retrieve()
                .bodyToMono(ServeData.class)
                .flatMap(serverStatus -> ServerResponse
                        .ok()
                        .contentType(APPLICATION_JSON)
                        .bodyValue(serverStatus));

    }
}
