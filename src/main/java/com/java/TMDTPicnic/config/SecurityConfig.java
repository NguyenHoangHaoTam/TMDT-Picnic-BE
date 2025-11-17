package com.java.TMDTPicnic.config;

import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**", // gom luôn login, logout, register,...
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
//            "/api/cart/**" // TẤT CẢ method (GET/POST/PUT/DELETE) với /api/cart/** đều PUBLIC
            "/api/shared-carts",
            "/api/addresses",
            "/api/group-buy",
            "/api/orders/vnpay-return",
            "/api/coupons"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/products/**",
            "/api/categories/**",
            "/api/group-buy/campaigns/active",
            "/api/reviews/product/**"
    };

    private static final String[] ADMIN_ENDPOINTS = {
            "/api/products/**",
            "/api/categories/**",
            "/api/users/**",
            "/api/dashboard/**",
            "/api/group-buy/campaigns/**",
            "/api/coupons/create"
    };
    private static final String[] AUTH_REQUIRED_ENDPOINTS = {
            "/api/addresses/**",
            "/api/group-buy/commit/**"
    };
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, ActiveUserFilter activeUserFilter) throws Exception {
        httpSecurity
                .authorizeHttpRequests(auth -> auth
                        // Public cho mọi method
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // GET không cần login
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()

                        // Admin được phép POST/PUT/DELETE
                        .requestMatchers(HttpMethod.POST, ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers(AUTH_REQUIRED_ENDPOINTS).authenticated()

                        // Các request còn lại -> cần login
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .addFilterAfter(activeUserFilter, BearerTokenAuthenticationFilter.class)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(),"HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("");

        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(converter);
        authConverter.setPrincipalClaimName("sub");
        return authConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
