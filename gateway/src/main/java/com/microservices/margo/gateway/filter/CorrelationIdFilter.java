package com.microservices.margo.gateway.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;

@Configuration
public class CorrelationIdFilter {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Bean
    public HandlerFilterFunction<?, ?> correlationIdFilter() {
        return (request, next) -> {
            String correlationId = request.headers().firstHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            ServerResponse response = next.handle(request);
            response.headers().add(CORRELATION_ID_HEADER, correlationId);
            return response;
        };
    }
}
