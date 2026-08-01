package com.example.gestionprojet.services;

import com.example.gestionprojet.dto.ActeurDto;
import com.example.gestionprojet.dto.FilmDto;
import com.example.gestionprojet.dto.HistoriqueDto;
import com.example.gestionprojet.entity.Acteur;
import com.example.gestionprojet.entity.Film;
import com.example.gestionprojet.entity.Genre;
import com.example.gestionprojet.entity.User;
import com.example.gestionprojet.entity.UserFilm;
import com.example.gestionprojet.repository.ActeurRepository;
import com.example.gestionprojet.repository.FilmRepository;
import com.example.gestionprojet.repository.GenreRepository;
import com.example.gestionprojet.repository.UserFilmRepository;
import com.example.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoriqueService {

    private final UserFilmRepository userFilmRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final TmdbService tmdbService;
    private final GenreRepository genreRepository;
    private final ActeurRepository acteurRepository;

    @Transactional
    public void enregistrerConsultation(String email, Long tmdbId) {
        log.info("➡️ enregistrerConsultation email={} tmdbId={}", email, tmdbId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found pour email=" + email));

        Film film = getOrCreateFilm(tmdbId);
        film.setNombreDeVues(
                (film.getNombreDeVues() == null ? 0 : film.getNombreDeVues()) + 1);
        filmRepository.save(film);

        Optional<UserFilm> existing =
                userFilmRepository.findByUserIdAndFilmImdbId(user.getId(), tmdbId);

        if (existing.isPresent()) {
            UserFilm uf = existing.get();
            uf.setDateConsultation(LocalDateTime.now());
            userFilmRepository.save(uf);
            log.info("✅ UserFilm mis à jour : id={} user={} film={}",
                    uf.getId(), user.getId(), film.getId());
        } else {
            UserFilm uf = UserFilm.builder()
                    .user(user)
                    .film(film)
                    .favoris(false)
                    .dateConsultation(LocalDateTime.now())
                    .build();
            UserFilm saved = userFilmRepository.save(uf);
            log.info("✅ UserFilm créé : id={} user={} film={}",
                    saved.getId(), user.getId(), film.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<HistoriqueDto> getHistorique(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found pour email=" + email));

        List<UserFilm> userFilms = userFilmRepository
                .findByUserIdOrderByDateConsultationDesc(user.getId());

        List<HistoriqueDto> result = new ArrayList<>();
        for (UserFilm uf : userFilms) {
            Film film = uf.getFilm();
            if (film == null) continue;

            HistoriqueDto dto = new HistoriqueDto();
            dto.setTmdbId(film.getImdbId());
            dto.setFilmId(film.getId());
            dto.setTitre(film.getTitre());
            dto.setSynopsis(film.getSynopsis());
            dto.setPosterUrl(film.getPosterUrl());
            dto.setBackdropUrl(film.getBackdropUrl());
            dto.setNote(film.getNote());
            dto.setDuree(film.getDuree());
            dto.setGenres(film.getGenres() != null
                    ? film.getGenres().stream().map(Genre::getNom).collect(java.util.stream.Collectors.toList())
                    : List.of());
            dto.setVideoDisponible(film.getVideoFileName() != null);
            dto.setDateConsultation(uf.getDateConsultation());
            result.add(dto);
        }
        return result;
    }

    // ✅ NOUVEAU — Enregistre la consultation d'un film CATALOGUE
    // (upload admin). Contrairement à enregistrerConsultation(), ne
    // passe JAMAIS par TMDB : le film est identifié directement par
    // son id interne (filmId), car les films ajoutés manuellement
    // n'ont pas de tmdbId (imdbId = null).
    @Transactional
    public void enregistrerConsultationCatalogue(String email, Long filmId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found pour email=" + email));

        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException(
                        "Film catalogue non trouvé, id=" + filmId));

        film.setNombreDeVues(
                (film.getNombreDeVues() == null ? 0 : film.getNombreDeVues()) + 1);
        filmRepository.save(film);

        Optional<UserFilm> existing =
                userFilmRepository.findByUserIdAndFilmId(user.getId(), filmId);

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

        log.info("✅ Consultation catalogue enregistrée : user={} film={}", user.getId(), filmId);
    }
    @Transactional
    public Film getOrCreateFilm(Long tmdbId) {
        Optional<Film> existing = filmRepository.findByImdbId(tmdbId);
        if (existing.isPresent()) return existing.get();

        FilmDto dto = tmdbService.getDetail(tmdbId);

        Film film = Film.builder()
                .imdbId(dto.getTmdbId())
                .titre(dto.getTitre())
                .synopsis(dto.getSynopsis())
                .note(dto.getNote())
                .duree(dto.getDuree())
                .posterUrl(dto.getPosterUrl())
                .backdropUrl(dto.getBackdropUrl())
                .nombreDeVues(0)
                .genres(resoudreGenres(dto.getGenres()))
                .acteurs(resoudreActeurs(dto.getActeurs()))
                .build();

        return filmRepository.save(film);
    }

    // ── Résout chaque nom de genre vers une entité Genre existante,
    // ou en crée une nouvelle si elle n'existe pas encore.
    //
    // NB : les genres TMDB n'ont pas de tmdbId fiable à ce stade
    // (parseFilm() dans TmdbService ne renvoie que les noms, pas les
    // ids), donc la résolution principale reste par nom (findByNom).
    // existsByTmdbId/findByTmdbId sont utilisés ici en complément :
    // si jamais un Genre existe déjà avec un tmdbId mais que son nom
    // a changé entre deux appels TMDB (rare, mais possible), on le
    // retrouve quand même au lieu de créer un doublon.
    private List<Genre> resoudreGenres(List<String> noms) {
        List<Genre> genres = new ArrayList<>();
        if (noms == null) return genres;

        for (String nom : noms) {
            if (nom == null || nom.isBlank()) continue;

            Optional<Genre> parNom = genreRepository.findByNom(nom);
            if (parNom.isPresent()) {
                genres.add(parNom.get());
                continue;
            }

            // Pas trouvé par nom : on vérifie si un tmdbId numérique
            // a été passé par erreur dans la liste de noms (cas
            // historique où genre_ids — des ids bruts — était mélangé
            // avec des noms). Si le "nom" est en fait un id TMDB déjà
            // connu, on réutilise le Genre correspondant.
            Integer tmdbIdPossible = tryParseInt(nom);
            if (tmdbIdPossible != null
                    && genreRepository.existsByTmdbId(tmdbIdPossible)) {
                Genre genreExistant = genreRepository
                        .findByTmdbId(tmdbIdPossible)
                        .orElseThrow(); // existsByTmdbId vient de confirmer sa présence
                genres.add(genreExistant);
                log.info("ℹ️ Genre retrouvé via tmdbId={} (nom brut '{}')",
                        tmdbIdPossible, nom);
                continue;
            }

            Genre nouveauGenre = genreRepository.save(
                    Genre.builder().nom(nom).build());
            log.info("✅ Nouveau genre créé : {}", nom);
            genres.add(nouveauGenre);
        }
        return genres;
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Résout chaque acteur par son tmdbId (imdbId en base). On
    // utilise existsByImdbId pour décider clairement si on est dans
    // un cas de création ou de mise à jour, et logguer chaque cas.
    private List<Acteur> resoudreActeurs(List<ActeurDto> dtos) {
        List<Acteur> acteurs = new ArrayList<>();
        if (dtos == null) return acteurs;

        for (ActeurDto a : dtos) {
            if (a.getTmdbId() == null) continue;

            boolean dejaConnu = acteurRepository.existsByImdbId(a.getTmdbId());

            Acteur acteur = acteurRepository.findByImdbId(a.getTmdbId())
                    .orElseGet(() -> Acteur.builder()
                            .imdbId(a.getTmdbId())
                            .nom(a.getNom())
                            .photo(a.getPhoto())
                            .build());

            // Le rôle (personnage) est spécifique à CE film : on le
            // met à jour à chaque fois, même si l'acteur existait déjà.
            acteur.setPersonnage(a.getPersonnage());
            Acteur saved = acteurRepository.save(acteur);

            log.info(dejaConnu
                            ? "♻️ Acteur réutilisé : {} (tmdbId={})"
                            : "✅ Nouvel acteur créé : {} (tmdbId={})",
                    a.getNom(), a.getTmdbId());

            acteurs.add(saved);
        }
        return acteurs;
    }

    @Transactional
    public String supprimerHistorique(String email, Long tmdbId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found pour email=" + email));

        Optional<UserFilm> existing =
                userFilmRepository.findByUserIdAndFilmImdbId(
                        user.getId(), tmdbId);

        if (existing.isPresent()) {
            userFilmRepository.delete(existing.get());
            return "Film supprimé de l'historique";
        }
        return "Film non trouvé dans l'historique";
    }

    @Transactional
    public String viderHistorique(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found pour email=" + email));

        List<UserFilm> historique = userFilmRepository
                .findByUserIdOrderByDateConsultationDesc(user.getId());

        userFilmRepository.deleteAll(historique);
        return "Historique vidé";
    }
}