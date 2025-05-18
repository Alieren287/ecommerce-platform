package com.alier.ecommerceproductservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = {
        "com.alier.ecommerceproductservice",
        "com.alier.ecommercewebcore.rest"  // Add this to scan the web-core controllers/advisors
})
public class EcommerceProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceProductServiceApplication.class, args);
    }

}
