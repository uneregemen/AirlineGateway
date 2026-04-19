package com.ecommerce.AirlineGateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

// SİHİRLİ IMPORT'LAR (uri filtresi eklendi)
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@SpringBootApplication
public class AirlineGatewayApplication {

	@Value("${BACKEND_URL:http://localhost:8080}")
	private String backendUrl;

	public static void main(String[] args) {
		SpringApplication.run(AirlineGatewayApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> gatewayRoutes() {
		return route("airline-main-api")
				.route(path("/**"), http())
				.before(uri(backendUrl))
				.build();
	}
}