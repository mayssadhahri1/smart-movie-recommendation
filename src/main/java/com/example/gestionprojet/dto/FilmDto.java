package com.example.gestionprojet.dto;

import lombok.Data;
import java.util.List;

@Data
public class FilmDto {
    private Long id;
    private Long tmdbId;
    private String titre;
    private String synopsis;
    private String note;
    private String duree;
    private String dateSortie;
    private String posterUrl;
    private String backdropUrl;
    private List<String> genres;
    private List<ActeurDto> acteurs;
    private String trailerUrl;
}