package com.erp.marks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MarksServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarksServiceApplication.class, args);
    }
}
