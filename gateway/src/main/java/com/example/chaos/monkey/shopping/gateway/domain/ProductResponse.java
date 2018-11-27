package com.example.chaos.monkey.shopping.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.example.chaos.monkey.shopping.domain.Product;

/**
 * @author Benjamin Wilms
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductResponse {

    private ResponseType responseType;
    private List<Product> products;
}
