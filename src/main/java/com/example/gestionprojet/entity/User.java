package com.example.gestionprojet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mot_passe", nullable = false)
    private String motPasse;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoleEnum role = RoleEnum.ROLE_CLIENT;

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_inscription")
    @Builder.Default
    private LocalDate dateInscription = LocalDate.now();

    @Column(name = "derniere_connexion")
    private LocalDate derniereConnexion;

    // Relation vers UserFilm pour favoris et historique
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UserFilm> userFilms;
}