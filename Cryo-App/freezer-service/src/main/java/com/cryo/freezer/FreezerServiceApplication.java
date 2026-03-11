package com.cryo.freezer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.cryo.freezer")
@EnableDiscoveryClient
@EnableScheduling
@EntityScan(basePackages = "com.cryo.freezer.entity")
@EnableJpaRepositories(basePackages = "com.cryo.freezer")
public class FreezerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreezerServiceApplication.class, args);
    }
}