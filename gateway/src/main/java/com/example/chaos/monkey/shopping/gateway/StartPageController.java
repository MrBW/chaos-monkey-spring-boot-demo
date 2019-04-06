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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Ryan Baxter, Benjamin Wilms
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


    @RequestMapping(value = {"/startpage/{version}"}, method = RequestMethod.GET)
    public Mono<Startpage> delegateStartpageRequest(@PathVariable Optional<String> version) {

        if (version.isPresent()) {
            if (version.get().equalsIgnoreCase("cb")) {
                return getStartpageCircuitBreaker();
            } else if (version.get().equalsIgnoreCase("lb")) {
                return getStartpageLoadBalanced();
            }
        }


        Startpage fallbackStartpage = new Startpage();
        fallbackStartpage.setStatusFashion("unsupported");
        fallbackStartpage.setStatusHotDeals("unsupported");
        fallbackStartpage.setStatusToys("unsupported");
        return Mono.just(fallbackStartpage);

    }


    private Mono<Startpage> getStartpageCircuitBreaker() {
        long start = System.currentTimeMillis();

        Span newSpan = this.tracer.nextSpan().name("allProductsCircuitBreaker");
        newSpan.tag("circuit.breaker", "true");
        newSpan.tag("load-balanced", "false");

        try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(newSpan.start())) {

            Mono<ProductResponse> hotdeals = webClient.get().uri("/cb/hotdeals").exchange().flatMap(responseProcessor)
                    .doOnError(t -> {
                        System.out.println("on error");
                    })
                    .onErrorResume(t -> {
                        System.out.println("on error resume");
                        t.printStackTrace();
                        return Mono.just(errorResponse);
                    });
            Mono<ProductResponse> fashionBestSellers = webClient.get().uri("/cb/fashion/bestseller").exchange().flatMap(responseProcessor)
                    .onErrorResume(t -> {
                        if (t instanceof TimeoutException) {
                            newSpan.tag("failure", "timeout");
                        } else if (t instanceof ResponseStatusException) {
                            newSpan.tag("failure", "responseStatusException");
                        }

                        t.printStackTrace();
                        return Mono.just(errorResponse);
                    });
            Mono<ProductResponse> toysBestSellers = webClient.get().uri("/cb/toys/bestseller").exchange().flatMap(responseProcessor)
                    .onErrorResume(t -> {
                        t.printStackTrace();
                        return Mono.just(errorResponse);
                    });

            return aggregateResults(start, hotdeals, fashionBestSellers, toysBestSellers);
        } finally {
            newSpan.finish();
        }


    }


    private Mono<Startpage> getStartpageLoadBalanced() {
        long start = System.currentTimeMillis();


        Span continuedSpan = this.tracer.toSpan(this.tracer.currentSpan().context());

        try {

            continuedSpan.name("allProductsLoadBalanced");
            continuedSpan.tag("load.balanced", "true");
            continuedSpan.tag("circuit.breaker", "true");


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

        } finally {
            continuedSpan.flush();
        }
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


    private ParameterizedTypeReference<Product> productParameterizedTypeReference =
            new ParameterizedTypeReference<Product>() {
            };

    private Function<ClientResponse, Mono<ProductResponse>> responseProcessor = clientResponse -> {
        HttpHeaders headers = clientResponse.headers().asHttpHeaders();

        if (headers.containsKey("fallback") && headers.get("fallback").contains("true")) {
            this.tracer.currentSpan().tag("failure", "fallback");

            return Mono.just(new ProductResponse(ResponseType.FALLBACK, Collections.emptyList()));

        } else if (clientResponse.statusCode().isError()) {
            this.tracer.currentSpan().tag("failure", "error");
            // HTTP Error Codes are not handled by Hystrix!?
            return Mono.just(new ProductResponse(ResponseType.ERROR, Collections.emptyList()));
        }

        return clientResponse.bodyToFlux(productParameterizedTypeReference).collectList()
                .flatMap(products -> Mono.just(new ProductResponse(ResponseType.REMOTE_SERVICE, products)));
    };
}
