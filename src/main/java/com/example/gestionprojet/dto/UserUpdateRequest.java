package com.example.gestionprojet.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nom;
    private String email;
    private String role;
}