package com.java.TMDTPicnic.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {
    private String payUrl;
    private String returnUrl;
    private String tmnCode;
    private String secretKey;
    private String apiUrl;
}