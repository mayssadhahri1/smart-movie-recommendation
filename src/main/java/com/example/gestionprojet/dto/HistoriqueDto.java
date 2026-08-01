package com.example.gestionprojet.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueDto {
    private Long tmdbId;
    private Long filmId;              // ✅ NOUVEAU — id interne, pour les films catalogue
    private String titre;
    private String synopsis;          // ✅ NOUVEAU
    private String posterUrl;
    private String backdropUrl;       // ✅ NOUVEAU
    private String note;
    private String duree;             // ✅ NOUVEAU
    private List<String> genres;      // ✅ NOUVEAU
    private Boolean videoDisponible;  // ✅ NOUVEAU — true si film catalogue avec vidéo
    private LocalDateTime dateConsultation;
}