package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.ActeurDto;
import com.example.gestionprojet.dto.FilmDto;
import com.example.gestionprojet.services.TmdbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/films")
@RequiredArgsConstructor
@Tag(name = "🎬 Films", description = "Endpoints TMDB — Films")
public class FilmController {

    private final TmdbService tmdbService;

    @Operation(summary = "Films tendances de la semaine")
    @GetMapping("/trending")
    public ResponseEntity<List<FilmDto>> trending() {
        return ResponseEntity.ok(tmdbService.getTrending());
    }

    @Operation(summary = "Films populaires")
    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> popular() {
        return ResponseEntity.ok(tmdbService.getPopular());
    }

    @Operation(summary = "Films les mieux notés")
    @GetMapping("/top-rated")
    public ResponseEntity<List<FilmDto>> topRated() {
        return ResponseEntity.ok(tmdbService.getTopRated());
    }

    @Operation(summary = "Rechercher un film")
    @GetMapping("/search")
    public ResponseEntity<List<FilmDto>> search(
            @RequestParam String query) {
        return ResponseEntity.ok(tmdbService.search(query));
    }

    @Operation(summary = "Détail d'un film")
    @GetMapping("/{tmdbId}")
    public ResponseEntity<FilmDto> detail(
            @PathVariable Long tmdbId) {
        return ResponseEntity.ok(tmdbService.getDetail(tmdbId));
    }

    @Operation(summary = "Acteurs d'un film")
    @GetMapping("/{tmdbId}/acteurs")
    public ResponseEntity<List<ActeurDto>> acteurs(
            @PathVariable Long tmdbId) {
        return ResponseEntity.ok(tmdbService.getActeurs(tmdbId));
    }

    @Operation(summary = "Trailer YouTube d'un film")
    @GetMapping("/{tmdbId}/trailer")
    public ResponseEntity<String> trailer(
            @PathVariable Long tmdbId) {
        return ResponseEntity.ok(tmdbService.getTrailer(tmdbId));
    }

    @Operation(summary = "Films par genre")
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<FilmDto>> byGenre(
            @PathVariable int genreId) {
        return ResponseEntity.ok(tmdbService.getByGenre(genreId));
    }
}