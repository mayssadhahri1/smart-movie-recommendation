package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.ProfilDto;
import com.example.gestionprojet.services.ProfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profil")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "👤 Profil", description = "Profil utilisateur connecté")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfilController {

    private final ProfilService profilService;

    @Operation(summary = "Voir mon profil",
            description = "Infos + stats + genres préférés")
    @GetMapping
    public ResponseEntity<ProfilDto> getProfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                profilService.getProfil(userDetails.getUsername()));
    }
}