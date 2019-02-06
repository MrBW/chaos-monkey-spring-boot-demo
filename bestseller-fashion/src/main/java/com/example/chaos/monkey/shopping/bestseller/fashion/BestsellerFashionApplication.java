package com.example.chaos.monkey.shopping.bestseller.fashion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class BestsellerFashionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BestsellerFashionApplication.class, args);
	}
}
