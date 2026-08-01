package com.example.gestionprojet.repository;

import com.example.gestionprojet.entity.UserFilm;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserFilmRepository extends JpaRepository<UserFilm, Long> {

    @EntityGraph(attributePaths = {"film"})
    List<UserFilm> findByUserIdAndFavorisTrue(Long userId);

    // ✅ NOUVEAU — tout l'historique (favoris + simples vues), utilisé
    // pour calculer les recommandations catalogue sur un signal plus large
    @EntityGraph(attributePaths = {"film"})
    List<UserFilm> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"film"})
    List<UserFilm> findByUserIdOrderByDateConsultationDesc(Long userId);

    @EntityGraph(attributePaths = {"film"})
    List<UserFilm> findAllByFavorisTrue();

    @Query("SELECT uf FROM UserFilm uf WHERE uf.user.id = :userId AND uf.film.imdbId = :tmdbId")
    Optional<UserFilm> findByUserIdAndFilmImdbId(
            @Param("userId") Long userId,
            @Param("tmdbId") Long tmdbId);

    long countByFavorisTrue();

    long countByFilmIdAndFavorisTrue(Long filmId);

    @EntityGraph(attributePaths = {"film", "user"})
    List<UserFilm> findTop10ByFavorisTrueOrderByDateConsultationDesc();

    Optional<UserFilm> findByUserIdAndFilmId(Long userId, Long filmId);
}