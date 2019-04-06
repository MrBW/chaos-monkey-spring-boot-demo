package com.example.chaos.monkey.shopping.gateway;

import brave.Tracer;
import com.example.chaos.monkey.shopping.domain.Product;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * @author Benjamin Wilms
 */
@RestController
public class FallbackController {

    private Tracer tracer;

    public FallbackController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/fallback")
    public ResponseEntity<List<Product>> fallback() {

        tracer.currentSpan().tag("fallback", "true");

        System.out.println("fallback enabled");
        HttpHeaders headers = new HttpHeaders();
        headers.add("fallback", "true");
        return ResponseEntity.ok().headers(headers).body(Collections.emptyList());
    }
}
