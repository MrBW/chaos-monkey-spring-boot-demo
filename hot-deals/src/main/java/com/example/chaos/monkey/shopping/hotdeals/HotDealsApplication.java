package com.example.chaos.monkey.shopping.hotdeals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class HotDealsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotDealsApplication.class, args);
	}
}
