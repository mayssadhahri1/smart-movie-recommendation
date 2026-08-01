package com.example.gestionprojet.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilmManuelRequest {
    private String titre;
    private String synopsis;
    private String note;
    private String duree;
    private String dateSortie;   // format "yyyy-MM-dd"
    private String posterUrl;
    private String backdropUrl;
    private String genres;       // ex: "Action,Drame,Comédie"
}