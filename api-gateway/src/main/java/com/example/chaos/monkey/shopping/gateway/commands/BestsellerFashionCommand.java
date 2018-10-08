package com.example.chaos.monkey.shopping.gateway.commands;

import com.example.chaos.monkey.shopping.domain.Product;
import com.example.chaos.monkey.shopping.gateway.domain.ProductResponse;
import com.example.chaos.monkey.shopping.gateway.domain.ResponseType;
import com.netflix.hystrix.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * @author Benjamin Wilms
 */
public class BestsellerFashionCommand extends HystrixCommand<ProductResponse> {

    private static final Logger log = LoggerFactory.getLogger(HotDealsCommand.class);
    private final RestTemplate restTemplate;
    private final String url;

    public BestsellerFashionCommand(HystrixCommandGroupKey group, int timeout, RestTemplate restTemplate,
                                    String url) {

        super(Setter.withGroupKey(group).andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeout))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("fashionThreadPool"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(10).withMaximumSize(50).withAllowMaximumSizeToDivergeFromCoreSize(true).withMaxQueueSize(100)));

        this.restTemplate = restTemplate;
        this.url = url;
    }

    protected ProductResponse run() throws Exception {
        ProductResponse response = new ProductResponse();

        response.setProducts(restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Product>>() {
        }).getBody());

        response.setResponseType(ResponseType.REMOTE_SERVICE);

        return response;
    }

    @Override
    protected ProductResponse getFallback() {
        
        return new ProductResponse(ResponseType.FALLBACK,Collections.<Product>emptyList());
    }
}
