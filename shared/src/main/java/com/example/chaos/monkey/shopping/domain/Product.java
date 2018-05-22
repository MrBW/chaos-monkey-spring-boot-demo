package com.example.chaos.monkey.shopping.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Benjamin Wilms
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {

    private long id;
    private String name;
    private ProductCategory category;

}
