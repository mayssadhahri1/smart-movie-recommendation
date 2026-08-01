package com.example.gestionprojet.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    // Genre vient de TMDB, on stocke l'id TMDB aussi
    @Column(name = "tmdb_id")
    private Integer tmdbId;

    @ManyToMany(mappedBy = "genres")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Film> films;
}
