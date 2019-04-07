package com.example.chaos.monkey.shopping.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Benjamin Wilms
 */
@Configuration
public class RoutingConfiguration {

    @Value("${rest.endpoint.fashion}")
    private String urlFashion;

    @Value("${rest.endpoint.toys}")
    private String urlToys;

    @Value("${rest.endpoint.hotdeals}")
    private String urlHotDeals;

    @Value("${server.port}")
    private int serverPort;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                // Retry, Circuit Breaker routes
                .route("startpage-lb-hotdeals", p -> p.path("/lb/hotdeals**")
                        .filters(f -> f.retry(c -> c.setRetries(2).setSeries(HttpStatus.Series.SERVER_ERROR))
                                .hystrix(c -> c.setName("hotdeals").setFallbackUri("forward:/fallback"))
                                .rewritePath("(\\/lb)", ""))
                        .uri(urlHotDeals))

                .route("startpage-lb-fashion", p -> p.path("/lb/fashion/**")
                        .filters(f -> f.retry(c -> c.setRetries(2).setSeries(HttpStatus.Series.SERVER_ERROR))
                                .hystrix(c -> c.setName("fashion").setFallbackUri("forward:/fallback"))
                                .rewritePath("(\\/lb)", ""))
                        .uri(urlFashion))

                .route("startpage-lb-toys", p -> p.path("/lb/toys/**")
                        .filters(f -> f.retry(c -> c.setRetries(2).setSeries(HttpStatus.Series.SERVER_ERROR))
                                .hystrix(c -> c.setName("toys").setFallbackUri("forward:/fallback"))
                                .rewritePath("(\\/lb)", ""))
                        .uri(urlToys))
                .build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl("http://localhost:" + serverPort).build();
    }
}
