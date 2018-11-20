package com.example.chaos.monkey.shopping.gateway;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author Benjamin Wilms
 */
@Configuration
@EnableDiscoveryClient
public class ApiGatewayConfiguration {

    @LoadBalanced
    @Bean
    RestTemplate loadBalanceRestTemplate() {
        return new RestTemplate();
    }
}
