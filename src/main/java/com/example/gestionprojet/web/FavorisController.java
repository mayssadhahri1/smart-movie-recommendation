package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.FavorisDto;
import com.example.gestionprojet.services.FavorisService;
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
@RequestMapping("/api/favoris")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "❤️ Favoris", description = "Gestion des films favoris")
@SecurityRequirement(name = "Bearer Authentication")
public class FavorisController {

    private final FavorisService favorisService;

    @Operation(summary = "Ajouter un film aux favoris",
            description = "ex: tmdbId=19995 pour Avatar")
    @PostMapping("/{tmdbId}")
    public ResponseEntity<String> ajouter(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                favorisService.ajouterFavori(
                        userDetails.getUsername(), tmdbId));
    }

    @Operation(summary = "Retirer un film des favoris")
    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<String> retirer(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                favorisService.retirerFavori(
                        userDetails.getUsername(), tmdbId));
    }

    @Operation(summary = "Voir mes favoris",
            description = "Liste tous les films en favoris")
    @GetMapping
    public ResponseEntity<List<FavorisDto>> mesFavoris(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                favorisService.getMesFavoris(userDetails.getUsername()));
    }
}