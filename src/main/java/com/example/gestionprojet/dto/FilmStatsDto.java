package com.example.gestionprojet.dto;

import lombok.Data;

@Data
public class FilmStatsDto {
    private Long tmdbId;
    private String titre;
    private String posterUrl;
    private long nombreFavoris;
    private long nombreVues;
}