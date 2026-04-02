package com.restaurant.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    @Setter
    private List<String> allowedOrigins;
}
