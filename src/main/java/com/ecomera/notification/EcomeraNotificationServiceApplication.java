package com.ecomera.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EcomeraNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcomeraNotificationServiceApplication.class, args);
    }

}
