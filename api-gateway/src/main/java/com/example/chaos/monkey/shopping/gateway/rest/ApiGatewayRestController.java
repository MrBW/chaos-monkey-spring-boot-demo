package com.example.chaos.monkey.shopping.gateway.rest;

import com.example.chaos.monkey.shopping.domain.Product;
import com.example.chaos.monkey.shopping.gateway.commands.BestsellerFashionCommand;
import com.example.chaos.monkey.shopping.gateway.commands.BestsellerToysCommand;
import com.example.chaos.monkey.shopping.gateway.commands.HotDealsCommand;
import com.example.chaos.monkey.shopping.gateway.domain.ProductResponse;
import com.example.chaos.monkey.shopping.gateway.domain.ResponseType;
import com.example.chaos.monkey.shopping.gateway.domain.Startpage;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Benjamin Wilms
 */
@RestController
public class ApiGatewayRestController {

    private RestTemplate restTemplate;

    @Value("${rest.endpoint.fashion}")
    private String urlFashion;

    @Value("${rest.endpoint.toys}")
    private String urlToys;

    @Value("${rest.endpoint.hotdeals}")
    private String urlHotDeals;

    public ApiGatewayRestController() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    @GetMapping("/startpage")
    public Startpage getStartpage() {
        Startpage page = new Startpage();
        long start = System.currentTimeMillis();

        // Create Futures for requesting results
        Future<ProductResponse> bestsellerFashionFuture = getBestsellerFashion();
        Future<ProductResponse> bestsellerToysFuture = getBestsellerToys();
        Future<ProductResponse> hotDealsFuture = getHotDeals();

        // Get Responses from Futures
        page.setFashionResponse(extractResponse(bestsellerFashionFuture));
        page.setToysResponse(extractResponse(bestsellerToysFuture));
        page.setHotDealsResponse(extractResponse(hotDealsFuture));

        // Summary
        page.setStatusFashion(page.getFashionResponse().getResponseType().name());
        page.setStatusToys(page.getToysResponse().getResponseType().name());
        page.setStatusHotDeals(page.getHotDealsResponse().getResponseType().name());

        // Request duration
        page.setDuration(System.currentTimeMillis() - start);
        return page;
    }

    private ProductResponse extractResponse(Future<ProductResponse> responseFuture) {
        try {
            return responseFuture.get();
        } catch (InterruptedException e) {
            return new ProductResponse(ResponseType.ERROR, Collections.<Product>emptyList());
        } catch (ExecutionException e) {
            return new ProductResponse(ResponseType.ERROR, Collections.<Product>emptyList());
        }
    }


    private Future<ProductResponse> getHotDeals() {
        return new HotDealsCommand(HystrixCommandGroupKey.Factory.asKey("hotdeals"), 200, restTemplate, urlHotDeals).queue();
    }

    private Future<ProductResponse> getBestsellerToys() {
        return new BestsellerToysCommand(HystrixCommandGroupKey.Factory.asKey("toys"), 200, restTemplate, urlToys).queue();
    }

    private Future<ProductResponse> getBestsellerFashion() {
        return new BestsellerFashionCommand(HystrixCommandGroupKey.Factory.asKey("fashion"), 200, restTemplate, urlFashion).queue();
    }

}
