package com.example.gestionprojet.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String role;
    private String nom;
    private Long id;
}