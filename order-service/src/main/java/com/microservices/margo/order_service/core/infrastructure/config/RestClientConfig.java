package com.microservices.margo.order_service.core.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final CorrelationProperties correlationProperties;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestInterceptor(correlationIdInterceptor())
                .build();
    }

    private ClientHttpRequestInterceptor correlationIdInterceptor() {
        return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            String correlationId = MDC.get(correlationProperties.key());
            if (correlationId != null){
                request.getHeaders().set(correlationProperties.header(), correlationId);
            }
            return execution.execute(request, body);
        };
    }
}
