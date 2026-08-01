package com.example.gestionprojet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

/**
 * Configuration MVC :
 * Expose le dossier "uploads" comme ressource statique accessible via /uploads/**
 * Ainsi, les images stockées sur le disque sont accessibles dans Thymeleaf via th:src="@{/uploads/nomfichier}"
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;


    //Méthode qui déclare les dossiers accessibles depuis le navigateur
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toUri().toString(); //Indique où chercher ces fichiers sur le disque du serveur
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);         //Indique où chercher ces fichiers sur le disque du serveur
    }
}
