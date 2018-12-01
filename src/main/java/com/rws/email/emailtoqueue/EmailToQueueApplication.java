package com.rws.email.emailtoqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
public class EmailToQueueApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmailToQueueApplication.class, args);
    }
}

