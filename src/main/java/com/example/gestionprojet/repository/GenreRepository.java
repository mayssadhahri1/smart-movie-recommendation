package com.example.gestionprojet.repository;

import com.example.gestionprojet.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByNom(String nom);
    Optional<Genre> findByTmdbId(Integer tmdbId);
    boolean existsByTmdbId(Integer tmdbId);
}