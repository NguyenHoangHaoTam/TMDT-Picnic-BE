package com.java.TMDTPicnic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "frontend")
@Getter
@Setter
public class FrontendConfig {

    /**
     * Base URL of the public frontend (no trailing slash).
     */
    private String domain;

    public void setDomain(String domain) {
        this.domain = normalize(domain);
    }

    public String getDomain() {
        return domain;
    }

    public String buildPath(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        return domain + normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}

