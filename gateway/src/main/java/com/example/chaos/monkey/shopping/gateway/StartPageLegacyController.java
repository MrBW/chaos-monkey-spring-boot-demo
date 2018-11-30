package com.example.chaos.monkey.shopping.gateway;

import com.example.chaos.monkey.shopping.domain.Product;
import com.example.chaos.monkey.shopping.gateway.domain.ProductResponse;
import com.example.chaos.monkey.shopping.gateway.domain.ResponseType;
import com.example.chaos.monkey.shopping.gateway.domain.Startpage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Ryan Baxter
 */
@RestController
public class StartPageLegacyController {

    @Value("${rest.endpoint.fashion}")
    private String urlFashion;

    @Value("${rest.endpoint.toys}")
    private String urlToys;

    @Value("${rest.endpoint.hotdeals}")
    private String urlHotDeals;

    private RestTemplate restClient;

    public StartPageLegacyController() {

        this.restClient = new RestTemplate();

    }

    private ParameterizedTypeReference<Product> productParameterizedTypeReference =
            new ParameterizedTypeReference<Product>() {
            };

    @GetMapping("/startpage/legacy")
    public Startpage getStartpageLegacy() {

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

        return page;

    }


    private ProductResponse getProductResponse(String url) {
        ProductResponse response = new ProductResponse();

        response.setProducts(restClient.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {
        }).getBody());

        response.setResponseType(ResponseType.REMOTE_SERVICE);

        return response;
    }
}
