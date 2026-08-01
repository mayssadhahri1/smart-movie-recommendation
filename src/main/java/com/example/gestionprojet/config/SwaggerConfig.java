package com.example.gestionprojet.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🎬 Smart Movie Platform API")
                        .description("""
                    API REST — PFA Smart Movie Recommendation Platform
                    
                    ## Authentification
                    1. Utilisez **/api/auth/login** pour obtenir un token JWT
                    2. Cliquez sur **Authorize** et entrez : `Bearer <votre_token>`
                    3. Testez tous les endpoints
                    """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Smart Movie Team")
                                .email("contact@smartmovie.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Entre ton token JWT ici")));
    }
}