package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.*;
import com.example.gestionprojet.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "👑 Admin", description = "Dashboard et gestion administrateur")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Dashboard — Stats générales")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @Operation(summary = "Liste tous les utilisateurs")
    @GetMapping("/users")
    public ResponseEntity<List<UserAdminDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(summary = "Rechercher un utilisateur")
    @GetMapping("/users/search")
    public ResponseEntity<List<UserAdminDto>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(adminService.searchUsers(query));
    }

    @Operation(summary = "Voir profil d'un utilisateur")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserAdminDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @Operation(summary = "Modifier un utilisateur")
    @PutMapping("/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    @Operation(summary = "Activer ou Désactiver un compte")
    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<String> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }

    @Operation(summary = "Top 10 films les plus consultés")
    @GetMapping("/films/top-vues")
    public ResponseEntity<List<FilmStatsDto>> topVues() {
        return ResponseEntity.ok(adminService.getTopFilmsConsultes());
    }

    @Operation(summary = "Top 10 films les plus aimés")
    @GetMapping("/films/top-favoris")
    public ResponseEntity<List<FilmStatsDto>> topFavoris() {
        return ResponseEntity.ok(adminService.getTop10FilmsAimes());
    }

    @Operation(summary = "Genres populaires")
    @GetMapping("/stats/genres")
    public ResponseEntity<?> genresPopulaires() {
        return ResponseEntity.ok(adminService.getGenresPopulaires());
    }

    @Operation(summary = "Inscriptions par semaine")
    @GetMapping("/stats/inscriptions")
    public ResponseEntity<?> inscriptions() {
        return ResponseEntity.ok(adminService.getInscriptionsParSemaine());
    }

    @Operation(summary = "Journal des 10 dernières connexions")
    @GetMapping("/connexions")
    public ResponseEntity<List<UserAdminDto>> dernieresConnexions() {
        return ResponseEntity.ok(adminService.getLastConnexions());
    }

    @Operation(summary = "Activité récente de la plateforme")
    @GetMapping("/activite")
    public ResponseEntity<List<ActivityDto>> activiteRecente() {
        return ResponseEntity.ok(adminService.getActiviteRecente());
    }

    // ══════════════════════════════════════════════════════════
    // 🎬 CATALOGUE PLATEFORME — CRUD COMPLET
    // ══════════════════════════════════════════════════════════

    @Operation(summary = "Liste des films actuellement dans le catalogue plateforme")
    @GetMapping("/films/plateforme")
    public ResponseEntity<List<AdminFilmDto>> filmsPlateforme() {
        return ResponseEntity.ok(adminService.getFilmsPlateforme());
    }

    @Operation(summary = "Détail complet d'un film (pour édition)")
    @GetMapping("/films/plateforme/{id}")
    public ResponseEntity<FilmCatalogueDto> getFilmDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getFilmPlateformeDetail(id));
    }

    @Operation(summary = "Ajouter un film manuellement avec upload direct du mp4",
            description = "L'admin saisit les infos du film et choisit un fichier vidéo depuis son PC")
    @PostMapping(value = "/films/manuel", consumes = "multipart/form-data")
    public ResponseEntity<?> ajouterFilmManuel(
            @RequestParam String titre,
            @RequestParam(required = false) String synopsis,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String duree,
            @RequestParam(required = false) String dateSortie,
            @RequestParam(required = false) String posterUrl,
            @RequestParam(required = false) String backdropUrl,
            @RequestParam(required = false) String genres,
            @RequestParam("file") MultipartFile file) {

        try {
            FilmManuelRequest request = new FilmManuelRequest(
                    titre, synopsis, note, duree, dateSortie, posterUrl, backdropUrl, genres);
            FilmCatalogueDto dto = adminService.ajouterFilmManuel(request, file);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erreur lors de l'enregistrement de la vidéo");
        }
    }

    @Operation(summary = "Modifier un film du catalogue (infos + vidéo optionnelle)",
            description = "Si aucun fichier n'est envoyé, la vidéo existante est conservée")
    @PutMapping(value = "/films/manuel/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> modifierFilm(
            @PathVariable Long id,
            @RequestParam String titre,
            @RequestParam(required = false) String synopsis,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String duree,
            @RequestParam(required = false) String dateSortie,
            @RequestParam(required = false) String posterUrl,
            @RequestParam(required = false) String backdropUrl,
            @RequestParam(required = false) String genres,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            FilmManuelRequest request = new FilmManuelRequest(
                    titre, synopsis, note, duree, dateSortie, posterUrl, backdropUrl, genres);
            FilmCatalogueDto dto = adminService.modifierFilm(id, request, file);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erreur lors de l'enregistrement de la vidéo");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Supprimer définitivement un film du catalogue",
            description = "Supprime le film en base ET le fichier vidéo sur le disque. Action irréversible.")
    @DeleteMapping("/films/plateforme/{id}")
    public ResponseEntity<String> supprimerFilm(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.supprimerFilmDefinitivement(id));
    }

    @Operation(summary = "Statut TMDB API",
            description = "true = opérationnelle, false = erreur")
    @GetMapping("/tmdb/status")
    public ResponseEntity<Boolean> tmdbStatus() {
        return ResponseEntity.ok(adminService.checkTmdb());
    }


}