package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.FilmCatalogueDto;
import com.example.gestionprojet.services.AdminService;
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
@RequestMapping("/api/films")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "🎬 Films", description = "Catalogue des films disponibles côté client")
public class FilmPublicController {

    private final AdminService adminService;

    @Operation(summary = "Catalogue des films ajoutés par l'admin")
    @GetMapping("/catalogue")
    public ResponseEntity<List<FilmCatalogueDto>> getCatalogue() {
        return ResponseEntity.ok(adminService.getCatalogue());
    }

    @Operation(summary = "Incrémenter le compteur de vues d'un film catalogue",
            description = "Incrémente le compteur global ET enregistre la vue dans l'historique personnel de l'utilisateur connecté (utilisé par les recommandations)")
    @PostMapping("/catalogue/{id}/vue")
    public ResponseEntity<Void> enregistrerVue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            adminService.incrementerVuesCatalogue(id);
            // ✅ Si l'utilisateur est connecté, on enregistre aussi dans SON historique
            // pour que les recommandations en tiennent compte
            if (userDetails != null) {
                adminService.enregistrerVueUtilisateur(id, userDetails.getUsername());
            }
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Ajouter/Retirer un film catalogue des favoris (toggle)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/catalogue/{id}/favori")
    public ResponseEntity<?> toggleFavori(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        try {
            String message = adminService.toggleFavoriCatalogue(id, userDetails.getUsername());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Vérifier si un film catalogue est en favori pour l'utilisateur connecté")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/catalogue/{id}/favori")
    public ResponseEntity<?> isFavori(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        try {
            return ResponseEntity.ok(adminService.isFavoriCatalogue(id, userDetails.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}