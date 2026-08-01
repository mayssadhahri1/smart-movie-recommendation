package com.example.gestionprojet.repository;

import com.example.gestionprojet.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FilmRepository extends JpaRepository<Film, Long> {

    Optional<Film> findByImdbId(Long imdbId);

    boolean existsByImdbId(Long imdbId);

    List<Film> findTop10ByOrderByNombreDeVuesDesc();

    @Query("SELECT f FROM Film f JOIN f.userFilms uf WHERE uf.favoris = true GROUP BY f ORDER BY COUNT(uf) DESC")
    List<Film> findTop10MostFavorited();

    @Query("SELECT f FROM Film f JOIN f.genres g WHERE g.id = :genreId")
    List<Film> findByGenreId(@Param("genreId") Long genreId);

    List<Film> findByTitreContainingIgnoreCase(String titre);

    // ✅ NOUVEAU : catalogue affiché côté client (uniquement ce que l'admin a ajouté)
    List<Film> findByDisponiblePlateformeTrueOrderByDateAjoutPlateformeDesc();
}