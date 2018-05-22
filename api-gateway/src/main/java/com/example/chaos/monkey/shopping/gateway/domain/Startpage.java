package com.example.chaos.monkey.shopping.gateway.domain;

import com.example.chaos.monkey.shopping.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Benjamin Wilms
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Startpage {

    private long duration;
    private ProductResponse fashionResponse;
    private ProductResponse toysResponse;
    private ProductResponse hotDealsResponse;
}
