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
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
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

    private RestTemplate restClient;

    private ProductResponse errorResponse;
    private WebClient webClient;
    private Tracer tracer;

    public StartPageController(WebClient webClient, Tracer tracer) {
        this.webClient = webClient;
        this.tracer = tracer;
        this.restClient = new RestTemplate();

        this.errorResponse = new ProductResponse();
        errorResponse.setResponseType(ResponseType.ERROR);
        errorResponse.setProducts(Collections.emptyList());
    }


    @RequestMapping(value = {"/startpage", "/startpage/{version}"}, method = RequestMethod.GET)
    public Mono<Startpage> delegateStartpageRequest(@PathVariable Optional<String> version) {
        if (version.isPresent()) {
            if (version.get().equalsIgnoreCase("cb")) {
                return getStartpageCircuitBreaker();
            } else if (version.get().equalsIgnoreCase("lb")) {
                return getStartpageLoadBalanced();
            }
        }
        //default landing
        return getStartpageLegacy();

    }


    private Mono<Startpage> getStartpageCircuitBreaker() {
        long start = System.currentTimeMillis();

        Span newSpan = this.tracer.nextSpan().name("allProductsCircuitBreaker");
        newSpan.tag("circuit.breaker", "true");
        newSpan.tag("load-balanced", "false");

        try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(newSpan.start())) {

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

            return aggregateResults(start, hotdeals, fashionBestSellers, toysBestSellers, newSpan);
        } finally {
            newSpan.finish();
        }


    }


    private Mono<Startpage> getStartpageLoadBalanced() {
        long start = System.currentTimeMillis();

        Span newSpan = this.tracer.nextSpan().name("allProductsLoadBalanced");
        newSpan.tag("load.balanced", "true");
        newSpan.tag("circuit.breaker", "true");

        try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(newSpan.start())) {
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


            return aggregateResults(start, hotdeals, fashionBestSellers, toysBestSellers, newSpan);
        } finally {
            newSpan.finish();
        }
    }

    private Mono<Startpage> getStartpageLegacy() {

        Span newSpan = this.tracer.nextSpan().name("allProductsLegacy");
        newSpan.tag("circuit.breaker", "false");
        newSpan.tag("load.balanced", "false");

        try (Tracer.SpanInScope ws = this.tracer.withSpanInScope(newSpan.start())) {

            Startpage page = new Startpage();

            long start = System.currentTimeMillis();

            // Get Responses from remote services
            page.setFashionResponse(getProductResponse(urlFashion));
            page.setToysResponse(getProductResponse(urlToys));
            page.setHotDealsResponse(getProductResponse(urlHotDeals));

            // Summary
            page.setStatusFashion(page.getFashionResponse().getResponseType().name());
            page.setStatusToys(page.getToysResponse().getResponseType().name());
            page.setStatusHotDeals(page.getHotDealsResponse().getResponseType().name());

            // Request duration
            page.setDuration(System.currentTimeMillis() - start);

            newSpan.tag("product.size.fashion", String.valueOf(page.getFashionResponse().getProducts().size()));
            newSpan.tag("product.size.toys", String.valueOf(page.getToysResponse().getProducts().size()));
            newSpan.tag("product.size.hotdeals", String.valueOf(page.getHotDealsResponse().getProducts().size()));

            return Mono.just(page);
        } finally {
            newSpan.finish();
        }

    }

    private Mono<Startpage> aggregateResults(long start, Mono<ProductResponse> hotdeals, Mono<ProductResponse> fashionBestSellers, Mono<ProductResponse> toysBestSellers, Span newSpan) {
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

            newSpan.tag("product.size.fashion", String.valueOf(fashion.getProducts().size()));
            newSpan.tag("product.size.toys", String.valueOf(toys.getProducts().size()));
            newSpan.tag("product.size.hotdeals", String.valueOf(deals.getProducts().size()));

            return Mono.just(p);
        });
        return page;
    }


    private ProductResponse getProductResponse(String url) {
        ProductResponse response = new ProductResponse();

        response.setProducts(restClient.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {
        }).getBody());

        response.setResponseType(ResponseType.REMOTE_SERVICE);

        return response;
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
