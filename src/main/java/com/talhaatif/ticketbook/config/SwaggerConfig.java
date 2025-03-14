package com.talhaatif.ticketbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.api.title}")
    private String title;

    @Value("${swagger.api.version}")
    private String version;

    @Value("${swagger.api.description}")
    private String description;

    @Bean
    public OpenAPI customOpenAPI() {
        String bearerAuth = "Bearer Authentication";
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                )
                // Enable JWT Authentication in Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(bearerAuth))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(bearerAuth,
                                new SecurityScheme()
                                        .name(bearerAuth)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
