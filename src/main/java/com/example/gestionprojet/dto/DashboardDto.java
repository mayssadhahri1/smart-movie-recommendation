package com.example.gestionprojet.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardDto {
    private long totalUtilisateurs;
    private long totalFilmsConsultes;
    private long totalFavoris;
    private List<FilmStatsDto> top10FilmsAimes;
    private Map<String, Long> genresPopulaires;
    private Map<String, Long> inscriptionsParSemaine;
    private boolean tmdbOperationnel;
}