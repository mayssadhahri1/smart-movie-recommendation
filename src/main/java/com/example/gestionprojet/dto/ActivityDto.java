package com.example.gestionprojet.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityDto {
    private String type;
    private String message;
    private String utilisateur;
    private LocalDateTime date;
    private String icon;
}