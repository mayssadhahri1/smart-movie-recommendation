package com.example.gestionprojet.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFilmDto {
    private Long id;
    private Long tmdbId;
    private String titre;
    private String posterUrl;
    private Boolean videoDisponible;
    private Boolean disponiblePlateforme;
    private LocalDateTime dateAjoutPlateforme;
    private Integer nombreVues;
    private Long nombreFavoris;
}