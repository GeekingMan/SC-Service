package com.geek.sc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
//@EnableDiscoveryClient
//@EnableAsync
public class SCServiceApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SCServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SCServiceApplication.class, args);
    }
}
