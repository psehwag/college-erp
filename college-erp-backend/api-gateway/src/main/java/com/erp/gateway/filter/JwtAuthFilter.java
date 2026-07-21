package com.erp.gateway.filter;

import com.erp.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private static final Logger log = Logger.getLogger(JwtAuthFilter.class.getName());

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String username    = jwtUtil.extractUsername(token);
                String role        = jwtUtil.extractRole(token);
                String userId      = jwtUtil.extractUserId(token);
                String referenceId = jwtUtil.extractReferenceId(token);

                ServerHttpRequest modified = request.mutate()
                        .header("X-User-Name", username != null ? username : "")
                        .header("X-User-Role", role != null ? role : "")
                        .header("X-User-Id", userId != null ? userId : "")
                        .header("X-Reference-Id", referenceId != null ? referenceId : "")
                        .build();

                return chain.filter(exchange.mutate().request(modified).build());

            } catch (Exception e) {
                log.severe("JWT filter error: " + e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {}
}
