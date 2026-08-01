package com.example.gestionprojet.dto;

import lombok.Data;

@Data
public class ActeurDto {
    private Long tmdbId;
    private String nom;
    private String personnage;
    private String photo;
}