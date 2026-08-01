package com.example.gestionprojet.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProfilDto {
    private Long id;
    private String nom;
    private String email;
    private String role;
    private LocalDate dateInscription;
    private LocalDate derniereConnexion;
    private int totalFavoris;
    private int totalConsultations;
    private List<String> genresPreferes;
}