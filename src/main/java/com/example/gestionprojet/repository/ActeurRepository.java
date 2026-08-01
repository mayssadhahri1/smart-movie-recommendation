package com.example.gestionprojet.repository;
import com.example.gestionprojet.entity.Acteur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActeurRepository extends JpaRepository<Acteur, Long> {
    Optional<Acteur> findByImdbId(Long imdbId);
    boolean existsByImdbId(Long imdbId);
}