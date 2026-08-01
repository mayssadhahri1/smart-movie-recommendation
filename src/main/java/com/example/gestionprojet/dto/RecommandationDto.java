package com.example.gestionprojet.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommandationDto {
    private Long tmdbId;
    private String titre;
    private String posterUrl;
    private String note;
    private List<String> genres;
    private String raison;
}