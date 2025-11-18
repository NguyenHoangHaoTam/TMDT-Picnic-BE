package com.java.TMDTPicnic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${DOMAIN_FE}")
    private String domainFe;

    @Bean
public CorsFilter corsFilter() {
    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowCredentials(true);

    // Domain FE chính
    cors.addAllowedOrigin(domainFe);

    // Cho phép tất cả subdomain Vercel (quan trọng)
    cors.addAllowedOriginPattern("https://*.vercel.app");

    cors.addAllowedHeader("*");
    cors.addAllowedMethod("*");
    cors.setExposedHeaders(Arrays.asList("Set-Cookie"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return new CorsFilter(source);
}

}
