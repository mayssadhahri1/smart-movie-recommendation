package com.example.gestionprojet.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmCatalogueDto {
    private Long id;
    private Long tmdbId;
    private String titre;
    private String synopsis;
    private String note;
    private String duree;
    private LocalDate dateSortie;
    private String posterUrl;
    private String backdropUrl;
    private Boolean videoDisponible;
    private List<String> genres;
    private Integer nombreVues;
    private Long nombreFavoris;
    private String streamUrl;
    private String raison;
}