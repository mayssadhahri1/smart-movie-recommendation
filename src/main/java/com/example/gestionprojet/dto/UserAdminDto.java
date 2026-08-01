package com.example.gestionprojet.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserAdminDto {
    private Long id;
    private String nom;
    private String email;
    private String role;
    private boolean actif;
    private LocalDate dateInscription;
    private LocalDate derniereConnexion;
    private int totalFavoris;
    private int totalConsultations;
}