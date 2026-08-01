package com.example.gestionprojet.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "acteurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Acteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "imdb_id")
    private Long imdbId;

    @Column(nullable = false)
    private String nom;

    // URL photo profil TMDB
    private String photo;

    // Nom du personnage joué dans le film
    private String personnage;

    @ManyToMany(mappedBy = "acteurs")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Film> films;
}