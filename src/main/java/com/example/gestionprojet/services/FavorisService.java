package com.example.gestionprojet.services;

import com.example.gestionprojet.dto.FavorisDto;
import com.example.gestionprojet.dto.FilmDto;
import com.example.gestionprojet.entity.Film;
import com.example.gestionprojet.entity.User;
import com.example.gestionprojet.entity.UserFilm;
import com.example.gestionprojet.repository.FilmRepository;
import com.example.gestionprojet.repository.UserFilmRepository;
import com.example.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavorisService {

    private final UserFilmRepository userFilmRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final TmdbService tmdbService;

    @Transactional
    public String ajouterFavori(String email, Long tmdbId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
        return "Film ajouté aux favoris";
    }

    @Transactional
    public String retirerFavori(String email, Long tmdbId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<UserFilm> existing =
                userFilmRepository.findByUserIdAndFilmImdbId(
                        user.getId(), tmdbId);

        if (existing.isPresent()) {
            UserFilm uf = existing.get();
            uf.setFavoris(false);
            userFilmRepository.save(uf);
            return "Film retiré des favoris";
        }
        return "Film non trouvé dans les favoris";
    }

    @Transactional(readOnly = true)
    public List<FavorisDto> getMesFavoris(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserFilm> userFilms =
                userFilmRepository.findByUserIdAndFavorisTrue(user.getId());

        List<FavorisDto> result = new ArrayList<>();
        for (UserFilm uf : userFilms) {
            Film film = uf.getFilm();
            FavorisDto dto = new FavorisDto();
            dto.setTmdbId(film.getImdbId());
            dto.setTitre(film.getTitre());
            dto.setPosterUrl(film.getPosterUrl());
            dto.setNote(film.getNote());
            dto.setDateAjout(uf.getDateConsultation());
            result.add(dto);
        }
        return result;
    }

    @Transactional
    private Film getOrCreateFilm(Long tmdbId) {
        Optional<Film> existing = filmRepository.findByImdbId(tmdbId);
        if (existing.isPresent()) return existing.get();

        FilmDto dto = tmdbService.getDetail(tmdbId);
        Film film = Film.builder()
                .imdbId(dto.getTmdbId())
                .titre(dto.getTitre())
                .synopsis(dto.getSynopsis())
                .note(dto.getNote())
                .posterUrl(dto.getPosterUrl())
                .backdropUrl(dto.getBackdropUrl())
                .nombreDeVues(0)
                .build();
        return filmRepository.save(film);
    }
}