package com.example.gestionprojet.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlateformeFilmDto {
    private Long id;
    private String titre;
    private String synopsis;
    private String note;
    private String duree;
    private LocalDate dateSortie;
    private String posterUrl;
    private String backdropUrl;
    private List<String> genres;
    private Boolean videoDisponible;
    private String streamUrl;
    private Integer nombreDeVues;
}