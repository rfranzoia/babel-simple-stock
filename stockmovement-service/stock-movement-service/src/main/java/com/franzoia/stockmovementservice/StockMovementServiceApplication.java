package com.franzoia.stockmovementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StockMovementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockMovementServiceApplication.class, args);
    }

}
