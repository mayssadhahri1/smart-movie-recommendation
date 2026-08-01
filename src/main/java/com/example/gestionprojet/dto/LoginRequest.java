package com.example.gestionprojet.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String motPasse;
}