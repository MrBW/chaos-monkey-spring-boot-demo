package com.example.chaos.monkey.shopping.gateway;

import brave.Tracer;
import com.example.chaos.monkey.shopping.domain.Product;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

/**
 * @author Benjamin Wilms
 */
public class FallbackController {

    private Tracer tracer;

    public FallbackController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/fallback")
    public ResponseEntity<List<Product>> fallback(HystrixRuntimeException exception) {

        tracer.currentSpan().tag("fallback", "true");
        tracer.currentSpan().tag("failureType", exception != null ? exception.getFailureType().name() : "unknown");

        System.out.println("fallback enabled");
        HttpHeaders headers = new HttpHeaders();
        headers.add("fallback", "true");
        return ResponseEntity.ok().headers(headers).body(Collections.emptyList());
    }
}
