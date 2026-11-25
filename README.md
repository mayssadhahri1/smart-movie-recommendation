# 🎬 Smart Movie Platform — Backend Spring Boot

> API REST d'une plateforme de films : catalogue, recommandations personnalisées, favoris, historique, streaming vidéo et administration.
> Projet réalisé pendant mes stages chez **Cyber Kairouan** (stage d'été 2025 et stage PFA 2026), avec un dashboard **Angular** et une application mobile **Flutter**.

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?logo=swagger&logoColor=black)
![TMDB](https://img.shields.io/badge/API-TMDB-01B4E4?logo=themoviedatabase&logoColor=white)

---

## ✨ Fonctionnalités

- 🔐 **Authentification** : inscription et connexion avec **JWT**, connexion **Google**, rôles `ADMIN` / `CLIENT`
- 🎞️ **Catalogue** : films populaires, mieux notés, tendances, recherche et filtrage par genre (via l'**API TMDB**)
- 🎯 **Recommandations** : analyse de l'historique et des favoris de l'utilisateur, calcul de ses **3 genres préférés** et suggestions de films correspondants
- ❤️ **Favoris** et 🕒 **historique de visionnage**
- ▶️ **Streaming vidéo** des films de la plateforme avec **HTTP Range Requests** (`206 Partial Content`)
- 🛠️ **Administration** : upload de films, gestion des utilisateurs (activation/désactivation), tableau de bord avec statistiques (top vues, top favoris, genres, inscriptions)
- 📘 **Documentation interactive** de l'API avec Swagger / OpenAPI

## 🏗️ Architecture

```
Flutter (mobile client)  ─┐
                          ├──►  API REST Spring Boot  ──►  MySQL
Angular (dashboard admin) ┘          │
                                     ├──►  API TMDB (films, acteurs, bandes-annonces)
                                     └──►  Stockage disque des vidéos (uploads/)
```

Organisation du code (`com.example.gestionprojet`) :

| Package | Rôle |
|---|---|
| `web` | Contrôleurs REST : `Auth`, `Film`, `FilmPublic`, `FilmStream`, `Favoris`, `Historique`, `Recommandation`, `Profil`, `Admin`, `User` |
| `services` | Logique métier : `TmdbService`, `RecommandationService`, `FileStorageService`, `AdminService`… |
| `security` | `SecurityConfig`, `JwtFilter`, `JwtUtil`, `UserDetailsServiceImpl` |
| `entity` | `User`, `Film`, `Genre`, `Acteur`, `UserFilm`, `RoleEnum` |
| `repository` | Repositories Spring Data JPA |
| `dto` | Objets d'échange (requêtes / réponses de l'API) |

## 🔌 Principaux endpoints

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` · `/api/auth/login` · `/api/auth/google` | Inscription et connexion |
| `GET` | `/api/films/popular` · `/top-rated` · `/trending` · `/search` | Catalogue TMDB |
| `GET` | `/api/films/{tmdbId}` · `/{tmdbId}/acteurs` · `/{tmdbId}/trailer` | Détail, acteurs et bande-annonce |
| `GET` | `/api/films/{id}/stream` | Streaming vidéo (Range Requests) |
| `GET` | `/api/recommandations` | Films recommandés pour l'utilisateur |
| `GET/POST/DELETE` | `/api/favoris` · `/api/historique` | Favoris et historique |
| `GET` | `/api/admin/dashboard` · `/stats/genres` · `/stats/inscriptions` | Statistiques administrateur |

## 🚀 Lancer le projet

**Prérequis** : Java 17, Maven, MySQL, une clé API [TMDB](https://www.themoviedb.org/settings/api).

Les informations sensibles ne sont jamais écrites dans le code : elles sont lues dans des **variables d'environnement**.

| Variable | Obligatoire | Description |
|---|---|---|
| `TMDB_API_KEY` | ✅ | Clé de l'API TMDB |
| `JWT_SECRET` | ✅ | Clé de signature des jetons (32 caractères minimum) |
| `DB_URL` | — | URL JDBC (par défaut `jdbc:mysql://localhost:3307/movie_platform`) |
| `DB_USERNAME` / `DB_PASSWORD` | — | Identifiants MySQL (par défaut `root` / vide) |

```bash
# 1. Définir les variables (Linux / macOS)
export TMDB_API_KEY=votre_cle_tmdb
export JWT_SECRET=une_cle_longue_et_aleatoire_de_32_caracteres_minimum

#    Windows PowerShell
#    $env:TMDB_API_KEY="votre_cle_tmdb"; $env:JWT_SECRET="..."

# 2. Lancer l'API (port 8081)
./mvnw spring-boot:run

# 3. Documentation Swagger
#    http://localhost:8081/swagger-ui.html
```

## 🧩 Les autres parties de la plateforme

| Partie | Dépôt |
|---|---|
| 🖥️ Dashboard d'administration (Angular 17) | [smart-movie-admin](https://github.com/mayssadhahri1/smart-movie-admin) |
| 📱 Application mobile client (Flutter) | à venir |
| 🗺️ Vue d'ensemble de la plateforme | [smart-movie-recommendation-platform](https://github.com/mayssadhahri1/smart-movie-recommendation-platform) |

## 📚 Ce que j'ai appris

- Concevoir une API REST complète et sécurisée avec Spring Security et JWT
- Intégrer une API externe (TMDB) et mettre ses données en cache dans la base
- Servir des vidéos en streaming progressif avec les en-têtes HTTP `Range`
- Corriger des problèmes réels : `LazyInitializationException`, CORS pour le streaming, relations JPA

---

👩‍💻 **Mayssa Dhahri** — Élève-ingénieure en Génie Logiciel · [LinkedIn](https://www.linkedin.com/in/mayssa-dhahri/)
