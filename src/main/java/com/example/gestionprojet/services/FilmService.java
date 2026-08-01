package com.example.gestionprojet.services;

import com.example.gestionprojet.entity.Film;
import com.example.gestionprojet.entity.User;
import com.example.gestionprojet.entity.UserFilm;
import com.example.gestionprojet.repository.FilmRepository;
import com.example.gestionprojet.repository.UserFilmRepository;
import com.example.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final UserFilmRepository userFilmRepository;

    // ── Utilitaires ─────────────────────────────────────────
    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur non trouvé"));
    }

    private Film getOrCreateFilm(Long tmdbId) {
        return filmRepository.findByImdbId(tmdbId)
                .orElseGet(() -> filmRepository.save(
                        Film.builder()
                                .imdbId(tmdbId)
                                .titre("Film " + tmdbId)
                                .nombreDeVues(0)
                                .build()
                ));
    }

    // ── Favoris ─────────────────────────────────────────────
    public String addFavori(Long tmdbId) {
        User user = getCurrentUser();
        Film film = getOrCreateFilm(tmdbId);

        Optional<UserFilm> existing =
                userFilmRepository.findByUserIdAndFilmImdbId(
                        user.getId(), tmdbId);

        if (existing.isPresent()) {
            UserFilm uf = existing.get();
            uf.setFavoris(true);
            userFilmRepository.save(uf);
        } else {
            UserFilm uf = UserFilm.builder()
                    .user(user)
                    .film(film)
                    .favoris(true)
                    .dateConsultation(LocalDateTime.now())
                    .build();
            userFilmRepository.save(uf);
        }
        return "Ajouté aux favoris";
    }

    public String removeFavori(Long tmdbId) {
        User user = getCurrentUser();
        userFilmRepository
                .findByUserIdAndFilmImdbId(user.getId(), tmdbId)
                .ifPresent(uf -> {
                    uf.setFavoris(false);
                    userFilmRepository.save(uf);
                });
        return "Retiré des favoris";
    }

    public List<Long> getFavoris() {
        User user = getCurrentUser();
        return userFilmRepository
                .findByUserIdAndFavorisTrue(user.getId())
                .stream()
                .map(uf -> uf.getFilm().getImdbId())
                .toList();
    }

    public boolean isFavori(Long tmdbId) {
        User user = getCurrentUser();
        return userFilmRepository
                .findByUserIdAndFilmImdbId(user.getId(), tmdbId)
                .map(uf -> Boolean.TRUE.equals(uf.getFavoris()))
                .orElse(false);
    }

    // ── Historique ──────────────────────────────────────────
    public void addToHistorique(Long tmdbId) {
        User user = getCurrentUser();
        Film film = getOrCreateFilm(tmdbId);

        // Incrémenter les vues
        film.setNombreDeVues(film.getNombreDeVues() + 1);
        filmRepository.save(film);

        Optional<UserFilm> existing =
                userFilmRepository.findByUserIdAndFilmImdbId(
                        user.getId(), tmdbId);

        if (existing.isPresent()) {
            UserFilm uf = existing.get();
            uf.setDateConsultation(LocalDateTime.now());
            userFilmRepository.save(uf);
        } else {
            UserFilm uf = UserFilm.builder()
                    .user(user)
                    .film(film)
                    .favoris(false)
                    .dateConsultation(LocalDateTime.now())
                    .build();
            userFilmRepository.save(uf);
        }
    }

    public List<Long> getHistorique() {
        User user = getCurrentUser();
        return userFilmRepository
                .findByUserIdOrderByDateConsultationDesc(user.getId())
                .stream()
                .map(uf -> uf.getFilm().getImdbId())
                .toList();
    }

    // ── Top Films ───────────────────────────────────────────
    public List<Film> getTopFilms() {
        return filmRepository.findTop10ByOrderByNombreDeVuesDesc();
    }
}