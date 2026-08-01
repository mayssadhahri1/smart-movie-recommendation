package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.FilmCatalogueDto;
import com.example.gestionprojet.dto.RecommandationDto;
import com.example.gestionprojet.services.RecommandationService;
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
@RequestMapping("/api/recommandations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "🎯 Recommandations", description = "Films recommandés selon les préférences")
@SecurityRequirement(name = "Bearer Authentication")
public class RecommandationController {

    private final RecommandationService recommandationService;

    @Operation(summary = "Recommandations TMDB",
            description = "Films TMDB basés sur les genres des favoris")
    @GetMapping
    public ResponseEntity<List<RecommandationDto>> getRecommandations(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                recommandationService.getRecommandations(userDetails.getUsername()));
    }

    @Operation(summary = "Recommandations catalogue plateforme",
            description = "Films avec streaming complet, basés sur tout l'historique de visionnage (vues + favoris)")
    @GetMapping("/catalogue")
    public ResponseEntity<List<FilmCatalogueDto>> getRecommandationsCatalogue(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                recommandationService.getRecommandationsCatalogue(userDetails.getUsername()));
    }

    @Operation(summary = "Trailer d'un film catalogue",
            description = "Retourne le lien YouTube si le film provient de TMDB, sinon null")
    @GetMapping("/catalogue/{filmId}/trailer")
    public ResponseEntity<String> getTrailerCatalogue(@PathVariable Long filmId) {
        return ResponseEntity.ok(recommandationService.getTrailerCatalogue(filmId));
    }
}