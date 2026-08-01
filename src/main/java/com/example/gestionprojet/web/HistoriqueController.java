package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.HistoriqueDto;
import com.example.gestionprojet.services.HistoriqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historique")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "📋 Historique", description = "Historique des films consultés")
@SecurityRequirement(name = "Bearer Authentication")
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    @Operation(summary = "Enregistrer une consultation",
            description = "ex: tmdbId=19995 pour Avatar")
    @PostMapping("/{tmdbId}")
    public ResponseEntity<Void> enregistrer(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal UserDetails userDetails) {
        historiqueService.enregistrerConsultation(
                userDetails.getUsername(), tmdbId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Enregistrer une consultation de film catalogue",
            description = "Pour les films uploadés par l'admin (identifiés par filmId interne, pas tmdbId)")
    @PostMapping("/catalogue/{filmId}")
    public ResponseEntity<Void> enregistrerCatalogue(
            @PathVariable Long filmId,
            @AuthenticationPrincipal UserDetails userDetails) {
        historiqueService.enregistrerConsultationCatalogue(
                userDetails.getUsername(), filmId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Voir mon historique",
            description = "Liste tous les films consultés")
    @GetMapping
    public ResponseEntity<List<HistoriqueDto>> monHistorique(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                historiqueService.getHistorique(userDetails.getUsername()));
    }

    @Operation(summary = "Supprimer un film de l'historique")
    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<String> supprimer(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                historiqueService.supprimerHistorique(
                        userDetails.getUsername(), tmdbId));
    }

    @Operation(summary = "Vider tout l'historique")
    @DeleteMapping
    public ResponseEntity<String> vider(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                historiqueService.viderHistorique(userDetails.getUsername()));
    }
}