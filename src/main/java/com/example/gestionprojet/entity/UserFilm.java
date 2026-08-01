package com.example.gestionprojet.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_film")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFilm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "film_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Film film;

    @Column(name = "date_consultation")
    @Builder.Default
    private LocalDateTime dateConsultation = LocalDateTime.now();

    // Note donnée par l'utilisateur (1-10), nullable
    private Integer note;

    // true = film en favoris
    @Builder.Default
    private Boolean favoris = false;
}