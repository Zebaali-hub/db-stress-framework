package com.zebacodes.dbstress.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public OpenAPI dbStressOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Database Stress Testing Framework API")
                .version("0.1.0")
                .description("REST API for running concurrent JDBC stress tests and collecting live TPS/latency metrics."));
    }
}
