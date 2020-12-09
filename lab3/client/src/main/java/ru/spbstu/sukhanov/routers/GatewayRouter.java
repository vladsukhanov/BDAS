package ru.spbstu.sukhanov.routers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.spbstu.sukhanov.handlers.GatewayHandler;

@Component
@RequiredArgsConstructor
public class GatewayRouter {

    private final GatewayHandler gatewayHandler;

    @Bean
    public RouterFunction<ServerResponse> serverStatusRoute() {
        return RouterFunctions
                .route()
                .path("/server-data", b1 -> b1
                    .GET("", gatewayHandler::getServerStatus)
                )
                .build();
    }
}
