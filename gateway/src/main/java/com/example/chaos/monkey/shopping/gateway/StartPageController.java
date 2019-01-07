package com.example.chaos.monkey.shopping.gateway;

import brave.Span;
import brave.Tracer;
import com.example.chaos.monkey.shopping.domain.Product;
import com.example.chaos.monkey.shopping.gateway.domain.ProductResponse;
import com.example.chaos.monkey.shopping.gateway.domain.ResponseType;
import com.example.chaos.monkey.shopping.gateway.domain.Startpage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ClientResponseExtensionsKt;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.function.Function;

/**
 * @author Ryan Baxter
 */
@RestController
public class StartPageController {

    @Value("${rest.endpoint.fashion}")
    private String urlFashion;

    @Value("${rest.endpoint.toys}")
    private String urlToys;

    @Value("${rest.endpoint.hotdeals}")
    private String urlHotDeals;

    private ProductResponse errorResponse;
    private WebClient webClient;
    private Tracer tracer;

    public StartPageController(WebClient webClient, Tracer tracer) {
        this.webClient = webClient;
        this.tracer = tracer;

        this.errorResponse = new ProductResponse();
        errorResponse.setResponseType(ResponseType.ERROR);
        errorResponse.setProducts(Collections.emptyList());
    }

    private ParameterizedTypeReference<Product> productParameterizedTypeReference =
            new ParameterizedTypeReference<Product>() {
            };

    private Function<ClientResponse, Mono<ProductResponse>> responseProcessor = clientResponse -> {
        HttpHeaders headers = clientResponse.headers().asHttpHeaders();

        if (headers.containsKey("fallback") && headers.get("fallback").contains("true")) {
            this.tracer.currentSpan().tag("failure","fallback");

            return Mono.just(new ProductResponse(ResponseType.FALLBACK, Collections.emptyList()));

        } else if (clientResponse.statusCode().isError()) {
            this.tracer.currentSpan().tag("failure","error");
            // HTTP Error Codes are not handled by Hystrix!?
            return Mono.just(new ProductResponse(ResponseType.ERROR, Collections.emptyList()));
        }

        return clientResponse.bodyToFlux(productParameterizedTypeReference).collectList()
                .flatMap(products -> Mono.just(new ProductResponse(ResponseType.REMOTE_SERVICE, products)));
    };


    @GetMapping("/startpage")
    public Mono<Startpage> getStartpage() {
        long start = System.currentTimeMillis();

        Span newSpan = this.tracer.nextSpan().name("getAllProducts");
        try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(newSpan.start())) {

            newSpan.annotate("startpage");
            newSpan.tag("request", "startpage");
            Mono<ProductResponse> hotdeals = webClient.get().uri("/hotdeals").exchange().flatMap(responseProcessor)
                    .doOnError(t -> {
                        System.out.println("on error");
                    })
                    .onErrorResume(t -> {
                        System.out.println("on error resume");
                        t.printStackTrace();
                        return Mono.just(errorResponse);
                    });
            Mono<ProductResponse> fashionBestSellers = webClient.get().uri("/fashion/bestseller").exchange().flatMap(responseProcessor)
                    .onErrorResume(t -> {
                        if (t instanceof TimeoutException) {
                            newSpan.tag("failure", "timeout");
                        } else if (t instanceof ResponseStatusException) {
                            newSpan.tag("failure", "responseStatusException");
                        }

                        t.printStackTrace();
                        return Mono.just(errorResponse);
                    });
            Mono<ProductResponse> toysBestSellers = webClient.get().uri("/toys/bestseller").exchange().flatMap(responseProcessor)
                    .onErrorResume(t -> {
                        t.printStackTrace();
                        return Mono.just(errorResponse);
                    });

            return aggregateResults(start, hotdeals, fashionBestSellers, toysBestSellers);
        } finally {
            newSpan.finish();
        }


    }


    @GetMapping("/startpage/lb")
    public Mono<Startpage> getStartpageRetry() {
        long start = System.currentTimeMillis();
        Mono<ProductResponse> hotdeals = webClient.get().uri("/lb/hotdeals").exchange().flatMap(responseProcessor)
                .onErrorResume(t -> {
                    t.printStackTrace();
                    return Mono.just(errorResponse);
                });
        Mono<ProductResponse> fashionBestSellers = webClient.get().uri("/lb/fashion/bestseller").exchange().flatMap(responseProcessor)
                .onErrorResume(t -> {
                    t.printStackTrace();
                    return Mono.just(errorResponse);
                });
        Mono<ProductResponse> toysBestSellers = webClient.get().uri("/lb/toys/bestseller").exchange().flatMap(responseProcessor)
                .onErrorResume(t -> {
                    t.printStackTrace();
                    return Mono.just(errorResponse);
                });


        return aggregateResults(start, hotdeals, fashionBestSellers, toysBestSellers);
    }

    private Mono<Startpage> aggregateResults(long start, Mono<ProductResponse> hotdeals, Mono<ProductResponse> fashionBestSellers, Mono<ProductResponse> toysBestSellers) {
        Mono<Startpage> page = Mono.zip(hotdeals, fashionBestSellers, toysBestSellers).flatMap(t -> {
            Startpage p = new Startpage();
            ProductResponse deals = t.getT1();
            ProductResponse fashion = t.getT2();
            ProductResponse toys = t.getT3();
            p.setFashionResponse(fashion);
            p.setHotDealsResponse(deals);
            p.setToysResponse(toys);
            p.setStatusFashion(fashion.getResponseType().name());
            p.setStatusHotDeals(deals.getResponseType().name());
            p.setStatusToys(toys.getResponseType().name());
            // Request duration
            p.setDuration(System.currentTimeMillis() - start);

            return Mono.just(p);
        });
        return page;
    }
}
