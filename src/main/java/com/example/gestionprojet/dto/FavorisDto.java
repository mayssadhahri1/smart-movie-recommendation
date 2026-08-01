package com.example.gestionprojet.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavorisDto {
    private Long tmdbId;
    private String titre;
    private String posterUrl;
    private String note;
    private LocalDateTime dateAjout;
}