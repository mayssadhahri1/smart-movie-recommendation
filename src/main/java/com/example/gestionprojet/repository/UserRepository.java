package com.example.gestionprojet.repository;

import com.example.gestionprojet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nom, String email);
    List<User> findTop10ByOrderByDerniereConnexionDesc();
    List<User> findTop5ByOrderByDateInscriptionDesc();
}