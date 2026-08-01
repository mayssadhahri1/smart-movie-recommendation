package com.example.gestionprojet.services;

import com.example.gestionprojet.dto.*;
import com.example.gestionprojet.entity.*;
import com.example.gestionprojet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserFilmRepository userFilmRepository;
    private final FilmRepository filmRepository;
    private final TmdbService tmdbService;
    private final GenreRepository genreRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        DashboardDto dto = new DashboardDto();
        dto.setTotalUtilisateurs(userRepository.count());
        dto.setTotalFilmsConsultes(userFilmRepository.count());
        dto.setTotalFavoris(userFilmRepository.countByFavorisTrue());
        dto.setTop10FilmsAimes(getTop10FilmsAimes());
        dto.setGenresPopulaires(getGenresPopulaires());
        dto.setInscriptionsParSemaine(getInscriptionsParSemaine());
        dto.setTmdbOperationnel(checkTmdb());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<FilmStatsDto> getTop10FilmsAimes() {
        return filmRepository.findTop10MostFavorited()
                .stream()
                .map(film -> {
                    FilmStatsDto dto = new FilmStatsDto();
                    dto.setTmdbId(film.getImdbId());
                    dto.setTitre(film.getTitre());
                    dto.setPosterUrl(film.getPosterUrl());
                    dto.setNombreVues(film.getNombreDeVues());
                    long favCount = userFilmRepository
                            .countByFilmIdAndFavorisTrue(film.getId());
                    dto.setNombreFavoris(favCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getGenresPopulaires() {
        List<UserFilm> favoris = userFilmRepository.findAllByFavorisTrue();
        Map<String, Long> genreCount = new HashMap<>();
        int filmsSansGenre = 0;

        for (UserFilm uf : favoris) {
            if (uf.getFilm() == null) continue;
            List<Genre> genres = uf.getFilm().getGenres();
            if (genres == null || genres.isEmpty()) {
                filmsSansGenre++;
                continue;
            }
            for (Genre genre : genres) {
                String nom = genre.getNom();
                if (nom == null || nom.isBlank()) continue;
                genreCount.put(nom, genreCount.getOrDefault(nom, 0L) + 1);
            }
        }

        Map<String, Long> result = new LinkedHashMap<>();
        genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getInscriptionsParSemaine() {
        List<User> users = userRepository.findAll();
        Map<String, Long> result = new LinkedHashMap<>();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        for (User u : users) {
            if (u.getDateInscription() != null) {
                int week = u.getDateInscription().get(weekFields.weekOfYear());
                int year = u.getDateInscription().getYear();
                String key = "S" + week + "-" + year;
                result.put(key, result.getOrDefault(key, 0L) + 1);
            }
        }
        return result;
    }

    public boolean checkTmdb() {
        try {
            tmdbService.getTrending();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<UserAdminDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserAdminDto> searchUsers(String query) {
        return userRepository
                .findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream()
                .map(this::toUserAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserAdminDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toUserAdminDto(user);
    }

    @Transactional
    public String toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActif(!user.getActif());
        userRepository.save(user);
        return user.getActif() ? "Compte activé" : "Compte désactivé";
    }

    @Transactional
    public String deleteUser(Long id) {
        userRepository.deleteById(id);
        return "Utilisateur supprimé";
    }

    @Transactional(readOnly = true)
    public List<UserAdminDto> getLastConnexions() {
        return userRepository
                .findTop10ByOrderByDerniereConnexionDesc()
                .stream()
                .map(this::toUserAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FilmStatsDto> getTopFilmsConsultes() {
        return filmRepository
                .findTop10ByOrderByNombreDeVuesDesc()
                .stream()
                .map(film -> {
                    FilmStatsDto dto = new FilmStatsDto();
                    dto.setTmdbId(film.getImdbId());
                    dto.setTitre(film.getTitre());
                    dto.setPosterUrl(film.getPosterUrl());
                    dto.setNombreVues(film.getNombreDeVues());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private UserAdminDto toUserAdminDto(User user) {
        UserAdminDto dto = new UserAdminDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setActif(user.getActif());
        dto.setDateInscription(user.getDateInscription());
        dto.setDerniereConnexion(user.getDerniereConnexion());
        dto.setTotalFavoris(
                userFilmRepository.findByUserIdAndFavorisTrue(user.getId()).size());
        dto.setTotalConsultations(
                userFilmRepository.findByUserIdOrderByDateConsultationDesc(user.getId()).size());
        return dto;
    }

    @Transactional
    public String updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNom(request.getNom());
        user.setEmail(request.getEmail());
        if (request.getRole() != null) {
            user.setRole(RoleEnum.valueOf(request.getRole()));
        }
        userRepository.save(user);
        return "Utilisateur mis à jour";
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> getActiviteRecente() {
        List<ActivityDto> activities = new ArrayList<>();

        List<User> connexions = userRepository.findTop10ByOrderByDerniereConnexionDesc();
        for (User u : connexions) {
            if (u.getDerniereConnexion() != null) {
                ActivityDto dto = new ActivityDto();
                dto.setType("connexion");
                dto.setMessage("s'est connecté(e)");
                dto.setUtilisateur(u.getNom());
                dto.setDate(u.getDerniereConnexion().atStartOfDay());
                dto.setIcon("🔐");
                activities.add(dto);
            }
        }

        List<User> inscriptions = userRepository.findTop5ByOrderByDateInscriptionDesc();
        for (User u : inscriptions) {
            if (u.getDateInscription() != null) {
                ActivityDto dto = new ActivityDto();
                dto.setType("inscription");
                dto.setMessage("a créé un compte");
                dto.setUtilisateur(u.getNom());
                dto.setDate(u.getDateInscription().atStartOfDay());
                dto.setIcon("👤");
                activities.add(dto);
            }
        }

        List<UserFilm> favoris = userFilmRepository.findTop10ByFavorisTrueOrderByDateConsultationDesc();
        for (UserFilm uf : favoris) {
            ActivityDto dto = new ActivityDto();
            dto.setType("favori");
            dto.setMessage("a ajouté un film en favori");
            dto.setUtilisateur(uf.getUser().getNom());
            dto.setDate(uf.getDateConsultation());
            dto.setIcon("❤️");
            activities.add(dto);
        }

        activities.sort((a, b) -> {
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });

        return activities.stream().limit(15).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════
    // 🎬 CATALOGUE PLATEFORME
    // ══════════════════════════════════════════════════════════

    @Transactional
    public FilmCatalogueDto ajouterFilmPlateforme(Long tmdbId) {
        Film film = filmRepository.findByImdbId(tmdbId).orElse(null);
        var detail = tmdbService.getDetail(tmdbId);

        if (film == null) {
            film = new Film();
            film.setImdbId(tmdbId);
            film.setNombreDeVues(0);
        }

        film.setTitre(detail.getTitre());
        film.setSynopsis(detail.getSynopsis());
        film.setNote(detail.getNote());
        film.setDuree(detail.getDuree());
        film.setDateSortie(parseDate(detail.getDateSortie()));
        film.setPosterUrl(detail.getPosterUrl());
        film.setBackdropUrl(detail.getBackdropUrl());
        film.setGenres(resoudreGenres(detail.getGenres()));
        film.setDisponiblePlateforme(true);
        film.setDateAjoutPlateforme(LocalDateTime.now());

        Film saved = filmRepository.save(film);
        log.info("✅ Film '{}' (tmdbId={}) ajouté au catalogue plateforme", saved.getTitre(), tmdbId);

        return toFilmCatalogueDto(saved);
    }

    @Transactional
    public String uploaderVideo(Long filmId, MultipartFile file) throws IOException {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));

        if (film.getVideoFileName() != null) {
            fileStorageService.supprimerVideo(film.getVideoFileName());
        }

        String nomFichier = fileStorageService.sauvegarderVideo(file);
        film.setVideoFileName(nomFichier);
        filmRepository.save(film);

        log.info("✅ Vidéo uploadée pour '{}' : {}", film.getTitre(), nomFichier);
        return "Vidéo uploadée avec succès";
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.warn("⚠️ Date TMDB invalide, ignorée : '{}'", dateStr);
            return null;
        }
    }

    @Transactional
    public FilmCatalogueDto ajouterFilmManuel(FilmManuelRequest request, MultipartFile video) throws IOException {

        if (request.getTitre() == null || request.getTitre().isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (video == null || video.isEmpty()) {
            throw new IllegalArgumentException("Le fichier vidéo est obligatoire");
        }

        Film film = new Film();
        film.setImdbId(null);
        film.setTitre(request.getTitre());
        film.setSynopsis(request.getSynopsis());
        film.setNote(request.getNote());
        film.setDuree(request.getDuree());
        film.setDateSortie(parseDate(request.getDateSortie()));
        film.setPosterUrl(request.getPosterUrl());
        film.setBackdropUrl(request.getBackdropUrl());
        film.setNombreDeVues(0);

        if (request.getGenres() != null && !request.getGenres().isBlank()) {
            List<String> noms = Arrays.stream(request.getGenres().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
            film.setGenres(resoudreGenres(noms));
        }

        film.setDisponiblePlateforme(true);
        film.setDateAjoutPlateforme(LocalDateTime.now());

        String nomFichier = fileStorageService.sauvegarderVideo(video);
        film.setVideoFileName(nomFichier);

        Film saved = filmRepository.save(film);
        log.info("✅ Film ajouté manuellement '{}' avec vidéo '{}'", saved.getTitre(), nomFichier);

        return toFilmCatalogueDto(saved);
    }

    @Transactional(readOnly = true)
    public FilmCatalogueDto getFilmPlateformeDetail(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));
        return toFilmCatalogueDto(film);
    }

    @Transactional
    public FilmCatalogueDto modifierFilm(Long id, FilmManuelRequest request, MultipartFile video) throws IOException {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));

        if (request.getTitre() == null || request.getTitre().isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }

        film.setTitre(request.getTitre());
        film.setSynopsis(request.getSynopsis());
        film.setNote(request.getNote());
        film.setDuree(request.getDuree());
        film.setDateSortie(parseDate(request.getDateSortie()));
        film.setPosterUrl(request.getPosterUrl());
        film.setBackdropUrl(request.getBackdropUrl());

        if (request.getGenres() != null && !request.getGenres().isBlank()) {
            List<String> noms = Arrays.stream(request.getGenres().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
            film.setGenres(resoudreGenres(noms));
        } else {
            film.setGenres(new ArrayList<>());
        }

        if (video != null && !video.isEmpty()) {
            if (film.getVideoFileName() != null) {
                fileStorageService.supprimerVideo(film.getVideoFileName());
            }
            String nomFichier = fileStorageService.sauvegarderVideo(video);
            film.setVideoFileName(nomFichier);
        }

        Film saved = filmRepository.save(film);
        log.info("✏️ Film modifié : '{}' (id={})", saved.getTitre(), id);

        return toFilmCatalogueDto(saved);
    }

    @Transactional
    public String supprimerFilmDefinitivement(Long id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));

        String titre = film.getTitre();

        if (film.getVideoFileName() != null) {
            fileStorageService.supprimerVideo(film.getVideoFileName());
        }

        filmRepository.delete(film);
        log.info("🗑️ Film supprimé définitivement : '{}' (id={})", titre, id);

        return "Film supprimé définitivement";
    }

    private List<Genre> resoudreGenres(List<String> nomsGenres) {
        List<Genre> genres = new ArrayList<>();
        if (nomsGenres == null) return genres;
        for (String nom : nomsGenres) {
            if (nom == null || nom.isBlank()) continue;
            Genre genre = genreRepository.findByNom(nom)
                    .orElseGet(() -> genreRepository.save(Genre.builder().nom(nom).build()));
            genres.add(genre);
        }
        return genres;
    }

    @Transactional
    public String retirerFilmPlateforme(Long filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));
        film.setDisponiblePlateforme(false);
        filmRepository.save(film);
        return "Film retiré du catalogue plateforme";
    }

    @Transactional(readOnly = true)
    public List<AdminFilmDto> getFilmsPlateforme() {
        return filmRepository.findByDisponiblePlateformeTrueOrderByDateAjoutPlateformeDesc()
                .stream()
                .map(f -> AdminFilmDto.builder()
                        .id(f.getId())
                        .tmdbId(f.getImdbId())
                        .titre(f.getTitre())
                        .posterUrl(f.getPosterUrl())
                        .videoDisponible(f.getVideoFileName() != null)
                        .disponiblePlateforme(f.getDisponiblePlateforme())
                        .dateAjoutPlateforme(f.getDateAjoutPlateforme())
                        .nombreVues(f.getNombreDeVues())
                        .nombreFavoris(userFilmRepository.countByFilmIdAndFavorisTrue(f.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FilmCatalogueDto> getCatalogue() {
        return filmRepository.findByDisponiblePlateformeTrueOrderByDateAjoutPlateformeDesc()
                .stream()
                .map(this::toFilmCatalogueDto)
                .collect(Collectors.toList());
    }

    @Value("${server.public-url:http://localhost:8081}")
    private String publicUrl;

    private FilmCatalogueDto toFilmCatalogueDto(Film f) {
        return FilmCatalogueDto.builder()
                .id(f.getId())
                .tmdbId(f.getImdbId())
                .titre(f.getTitre())
                .synopsis(f.getSynopsis())
                .note(f.getNote())
                .duree(f.getDuree())
                .dateSortie(f.getDateSortie())
                .posterUrl(f.getPosterUrl())
                .backdropUrl(f.getBackdropUrl())
                .videoDisponible(f.getVideoFileName() != null)
                .genres(f.getGenres() != null
                        ? f.getGenres().stream().map(Genre::getNom).collect(Collectors.toList())
                        : List.of())
                .nombreVues(f.getNombreDeVues())
                .nombreFavoris(userFilmRepository.countByFilmIdAndFavorisTrue(f.getId()))
                .streamUrl(f.getVideoFileName() != null
                        ? publicUrl + "/api/films/" + f.getId() + "/stream"
                        : null)
                .build();
    }

    // ══════════════════════════════════════════════════════════
    // 📊 VUES — Compteur global (affichage "X vues" sur la fiche)
    // ══════════════════════════════════════════════════════════
    @Transactional
    public void incrementerVuesCatalogue(Long filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));
        film.setNombreDeVues(film.getNombreDeVues() + 1);
        filmRepository.save(film);
    }

    // ══════════════════════════════════════════════════════════
    // ✅ NOUVEAU — Enregistre la vue DANS L'HISTORIQUE PERSONNEL
    // de l'utilisateur connecté, pour que les recommandations
    // puissent en tenir compte (pas seulement les favoris).
    // ══════════════════════════════════════════════════════════
    @Transactional
    public void enregistrerVueUtilisateur(Long filmId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));

        Optional<UserFilm> existing =
                userFilmRepository.findByUserIdAndFilmId(user.getId(), filmId);

        if (existing.isPresent()) {
            // On rafraîchit juste la date de consultation, sans toucher au statut favori
            UserFilm uf = existing.get();
            uf.setDateConsultation(LocalDateTime.now());
            userFilmRepository.save(uf);
        } else {
            UserFilm uf = UserFilm.builder()
                    .user(user)
                    .film(film)
                    .favoris(false)
                    .dateConsultation(LocalDateTime.now())
                    .build();
            userFilmRepository.save(uf);
        }
    }

    // ══════════════════════════════════════════════════════════
    // ❤️ FAVORIS — Pour les films catalogue (par id réel, pas tmdbId)
    // ══════════════════════════════════════════════════════════
    @Transactional
    public String toggleFavoriCatalogue(Long filmId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));

        Optional<UserFilm> existing = userFilmRepository.findByUserIdAndFilmId(user.getId(), filmId);

        if (existing.isPresent()) {
            UserFilm uf = existing.get();
            boolean nouveauStatut = !Boolean.TRUE.equals(uf.getFavoris());
            uf.setFavoris(nouveauStatut);
            userFilmRepository.save(uf);
            return nouveauStatut ? "Ajouté aux favoris" : "Retiré des favoris";
        } else {
            UserFilm uf = UserFilm.builder()
                    .user(user)
                    .film(film)
                    .favoris(true)
                    .dateConsultation(LocalDateTime.now())
                    .build();
            userFilmRepository.save(uf);
            return "Ajouté aux favoris";
        }
    }

    @Transactional(readOnly = true)
    public boolean isFavoriCatalogue(Long filmId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return userFilmRepository.findByUserIdAndFilmId(user.getId(), filmId)
                .map(uf -> Boolean.TRUE.equals(uf.getFavoris()))
                .orElse(false);
    }

    public List<FilmDto> rechercherTmdb(String query) {
        return tmdbService.search(query);
    }
}