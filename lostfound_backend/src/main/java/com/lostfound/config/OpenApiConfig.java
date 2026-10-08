package com.lostfound.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Adds an "Authorize" button to Swagger UI so you can paste a JWT once
// and then call protected endpoints straight from the browser.
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI lostFoundOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lost & Found API")
                        .version("v1")
                        .description("""
                                Backend for reporting lost and found items, claiming found items,
                                and receiving targeted notifications when a match appears.

                                To use protected endpoints: call /api/v1/auth/login, copy the
                                accessToken, then click Authorize and paste it.
                                """)
                        .contact(new Contact().name("Lost & Found")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
