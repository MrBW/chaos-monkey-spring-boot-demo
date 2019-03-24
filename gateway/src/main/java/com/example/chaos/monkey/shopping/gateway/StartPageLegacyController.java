package com.example.chaos.monkey.shopping.gateway;

import brave.Span;
import brave.Tracer;
import com.example.chaos.monkey.shopping.domain.Product;
import com.example.chaos.monkey.shopping.gateway.domain.ProductResponse;
import com.example.chaos.monkey.shopping.gateway.domain.ResponseType;
import com.example.chaos.monkey.shopping.gateway.domain.Startpage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Benjamin Wilms
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
    private Tracer tracer;

    public StartPageLegacyController(Tracer tracer) {
        this.tracer = tracer;
        restClient = new RestTemplate();
    }

    @RequestMapping(value = {"/legacy"}, method = RequestMethod.GET)
    public Startpage getStartpageLegacy() {

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

            return page;
        } finally {
            newSpan.finish();
        }

    }

    private ProductResponse getProductResponse(String url) {
        ProductResponse response = new ProductResponse();

        response.setProducts(restClient.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {
        }).getBody());

        response.setResponseType(ResponseType.REMOTE_SERVICE);

        return response;
    }

}
