package com.seshop;

import com.seshop.shared.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SeShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeShopApplication.class, args);
    }
}
