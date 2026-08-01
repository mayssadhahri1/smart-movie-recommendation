package com.example.gestionprojet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "films")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "imdb_id")
    private Long imdbId;

    @Column(nullable = false)
    private String titre;

    @Lob
    private String synopsis;

    private String note;

    private String duree;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @Column(name = "nombre_de_vues")
    @Builder.Default
    private Integer nombreDeVues = 0;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "backdrop_url")
    private String backdropUrl;

    // ✅ Nom du fichier vidéo stocké sur le serveur (streaming, pas d'URL externe)
    @Column(name = "video_file_name")
    private String videoFileName;

    @Column(name = "disponible_plateforme")
    @Builder.Default
    private Boolean disponiblePlateforme = false;

    @Column(name = "date_ajout_plateforme")
    private LocalDateTime dateAjoutPlateforme;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "film_genre",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "film_acteur",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "acteur_id")
    )
    private List<Acteur> acteurs;

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL)
    private List<UserFilm> userFilms;
}