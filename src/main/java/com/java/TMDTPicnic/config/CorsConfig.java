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

        // CHO PHÉP NHIỀU DOMAIN
        cors.setAllowedOrigins(Arrays.asList(
                domainFe,
                "https://deploy-tmdt-mcid-omutiy686-haotams-projects.vercel.app"  // FE thật hiện tại
        ));

        cors.addAllowedHeader("*");
        cors.addAllowedMethod("*");
        cors.setExposedHeaders(Arrays.asList("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return new CorsFilter(source);
    }
}
