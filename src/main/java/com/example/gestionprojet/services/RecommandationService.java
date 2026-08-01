package com.example.gestionprojet.services;

import com.example.gestionprojet.dto.FilmCatalogueDto;
import com.example.gestionprojet.dto.FilmDto;
import com.example.gestionprojet.dto.RecommandationDto;
import com.example.gestionprojet.entity.Film;
import com.example.gestionprojet.entity.Genre;
import com.example.gestionprojet.entity.User;
import com.example.gestionprojet.entity.UserFilm;
import com.example.gestionprojet.repository.FilmRepository;
import com.example.gestionprojet.repository.UserFilmRepository;
import com.example.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommandationService {

    private final UserRepository userRepository;
    private final UserFilmRepository userFilmRepository;
    private final FilmRepository filmRepository;
    private final TmdbService tmdbService;

    private static final Map<String, Integer> GENRE_IDS = new HashMap<>();
    static {
        GENRE_IDS.put("Action", 28);
        GENRE_IDS.put("Aventure", 12);
        GENRE_IDS.put("Animation", 16);
        GENRE_IDS.put("Comédie", 35);
        GENRE_IDS.put("Crime", 80);
        GENRE_IDS.put("Documentaire", 99);
        GENRE_IDS.put("Drame", 18);
        GENRE_IDS.put("Fantastique", 14);
        GENRE_IDS.put("Horreur", 27);
        GENRE_IDS.put("Romance", 10749);
        GENRE_IDS.put("Science-Fiction", 878);
        GENRE_IDS.put("Thriller", 53);
    }

    // ══════════════════════════════════════════════════════════
    // 🎯 RECOMMANDATIONS TMDB
    // ✅ CORRIGÉ : basées sur TOUT l'historique (vues + favoris),
    // pas seulement les favoris. Les favoris comptent double.
    // Top 3 genres au lieu d'un seul, pour plus de diversité.
    // ══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<RecommandationDto> getRecommandations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ tout l'historique (vues + favoris), pas seulement les favoris
        List<UserFilm> historique = userFilmRepository.findByUserId(user.getId());

        if (historique.isEmpty()) {
            return tmdbService.getTrending().stream()
                    .limit(10)
                    .map(this::toRecommandationDto)
                    .collect(Collectors.toList());
        }

        Map<String, Integer> genreCount = new HashMap<>();
        Set<Long> dejaVus = new HashSet<>();

        for (UserFilm uf : historique) {
            Film film = uf.getFilm();
            if (film == null) continue;

            int poids = Boolean.TRUE.equals(uf.getFavoris()) ? 2 : 1;

            // Film catalogue (genres stockés directement en base)
            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                for (Genre g : film.getGenres()) {
                    if (g.getNom() == null || g.getNom().isBlank()) continue;
                    genreCount.merge(g.getNom(), poids, Integer::sum);
                }
            }
            // Film TMDB (on va chercher les genres via l'API)
            else if (film.getImdbId() != null) {
                dejaVus.add(film.getImdbId());
                try {
                    FilmDto detail = tmdbService.getDetail(film.getImdbId());
                    for (String g : detail.getGenres()) {
                        genreCount.merge(g, poids, Integer::sum);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (genreCount.isEmpty()) {
            return tmdbService.getTrending().stream()
                    .limit(10)
                    .map(this::toRecommandationDto)
                    .collect(Collectors.toList());
        }

        // ✅ Top 3 genres préférés (au lieu d'un seul) pour plus de variété
        List<String> genresPreferes = genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<FilmDto> candidats = new ArrayList<>();
        Set<Long> idsAjoutes = new HashSet<>();
        for (String genre : genresPreferes) {
            Integer genreId = GENRE_IDS.get(genre);
            if (genreId == null) continue;
            for (FilmDto f : tmdbService.getByGenre(genreId)) {
                if (idsAjoutes.add(f.getTmdbId())) {
                    candidats.add(f);
                }
            }
        }

        List<RecommandationDto> result = candidats.stream()
                .filter(f -> !dejaVus.contains(f.getTmdbId()))
                .limit(10)
                .map(f -> {
                    RecommandationDto dto = toRecommandationDto(f);
                    String genreCorrespondant = (f.getGenres() != null)
                            ? f.getGenres().stream()
                              .filter(genresPreferes::contains)
                              .findFirst()
                              .orElse(genresPreferes.get(0))
                            : genresPreferes.get(0);
                    dto.setRaison("Parce que vous aimez " + genreCorrespondant);
                    return dto;
                })
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return tmdbService.getTrending().stream()
                    .limit(10)
                    .map(this::toRecommandationDto)
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<String> getGenresPreferes(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ tout l'historique, cohérent avec getRecommandations()
        List<UserFilm> historique = userFilmRepository.findByUserId(user.getId());

        Map<String, Integer> genreCount = new HashMap<>();
        for (UserFilm uf : historique) {
            Film film = uf.getFilm();
            if (film == null) continue;
            int poids = Boolean.TRUE.equals(uf.getFavoris()) ? 2 : 1;

            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                for (Genre g : film.getGenres()) {
                    if (g.getNom() == null) continue;
                    genreCount.merge(g.getNom(), poids, Integer::sum);
                }
            } else if (film.getImdbId() != null) {
                try {
                    FilmDto detail = tmdbService.getDetail(film.getImdbId());
                    for (String g : detail.getGenres()) {
                        genreCount.merge(g, poids, Integer::sum);
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private RecommandationDto toRecommandationDto(FilmDto f) {
        RecommandationDto dto = new RecommandationDto();
        dto.setTmdbId(f.getTmdbId());
        dto.setTitre(f.getTitre());
        dto.setPosterUrl(f.getPosterUrl());
        dto.setNote(f.getNote());
        dto.setGenres(f.getGenres());
        dto.setRaison("Tendance du moment");
        return dto;
    }

    // ══════════════════════════════════════════════════════════
    // 🎯 RECOMMANDATIONS CATALOGUE — basé sur TOUT l'historique
    // (films regardés + favoris), pas seulement les favoris.
    // Les favoris comptent double par rapport aux simples vues.
    // (déjà correct, inchangé)
    // ══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<FilmCatalogueDto> getRecommandationsCatalogue(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserFilm> consultations = userFilmRepository.findByUserId(user.getId());

        Map<String, Integer> genreCount = new HashMap<>();
        Set<Long> filmsDejaConsultes = new HashSet<>();

        for (UserFilm uf : consultations) {
            Film film = uf.getFilm();
            if (film == null) continue;
            filmsDejaConsultes.add(film.getId());

            int poids = Boolean.TRUE.equals(uf.getFavoris()) ? 2 : 1;

            boolean estFilmCatalogue = film.getGenres() != null && !film.getGenres().isEmpty();

            if (estFilmCatalogue) {
                for (Genre g : film.getGenres()) {
                    String nom = g.getNom();
                    if (nom == null || nom.isBlank()) continue;
                    genreCount.put(nom, genreCount.getOrDefault(nom, 0) + poids);
                }
            } else if (film.getImdbId() != null) {
                try {
                    FilmDto detail = tmdbService.getDetail(film.getImdbId());
                    for (String g : detail.getGenres()) {
                        genreCount.put(g, genreCount.getOrDefault(g, 0) + poids);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (genreCount.isEmpty()) {
            return filmRepository.findByDisponiblePlateformeTrueOrderByDateAjoutPlateformeDesc()
                    .stream()
                    .filter(f -> !filmsDejaConsultes.contains(f.getId()))
                    .limit(10)
                    .map(f -> toFilmCatalogueDtoAvecRaison(f, "Nouveauté sur la plateforme"))
                    .collect(Collectors.toList());
        }

        List<String> genresPreferes = genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Film> candidats = filmRepository.findByDisponiblePlateformeTrueOrderByDateAjoutPlateformeDesc();

        List<FilmCatalogueDto> result = candidats.stream()
                .filter(f -> !filmsDejaConsultes.contains(f.getId()))
                .filter(f -> f.getGenres() != null && f.getGenres().stream()
                        .anyMatch(g -> genresPreferes.contains(g.getNom())))
                .limit(10)
                .map(f -> {
                    String genreCorrespondant = f.getGenres().stream()
                            .map(Genre::getNom)
                            .filter(genresPreferes::contains)
                            .findFirst()
                            .orElse(genresPreferes.get(0));
                    return toFilmCatalogueDtoAvecRaison(f, "Parce que vous aimez " + genreCorrespondant);
                })
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            return filmRepository.findByDisponiblePlateformeTrueOrderByDateAjoutPlateformeDesc()
                    .stream()
                    .filter(f -> !filmsDejaConsultes.contains(f.getId()))
                    .limit(10)
                    .map(f -> toFilmCatalogueDtoAvecRaison(f, "Nouveauté sur la plateforme"))
                    .collect(Collectors.toList());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public String getTrailerCatalogue(Long filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));

        if (film.getImdbId() == null) {
            return null;
        }
        return tmdbService.getTrailer(film.getImdbId());
    }

    private FilmCatalogueDto toFilmCatalogueDtoAvecRaison(Film f, String raison) {
        return FilmCatalogueDto.builder()
                .id(f.getId())
                .tmdbId(f.getImdbId())
                .titre(f.getTitre())
                .synopsis(f.getSynopsis())
                .note(f.getNote())
                .duree(f.getDuree())
                .dateSortie(f.getDateSortie())
                .posterUrl(f.getPosterUrl())
                .backdropUrl(f.getBackdropUrl())
                .videoDisponible(f.getVideoFileName() != null)
                .genres(f.getGenres() != null
                        ? f.getGenres().stream().map(Genre::getNom).collect(Collectors.toList())
                        : List.of())
                .nombreVues(f.getNombreDeVues())
                .nombreFavoris(userFilmRepository.countByFilmIdAndFavorisTrue(f.getId()))
                .raison(raison)
                .build();
    }
}