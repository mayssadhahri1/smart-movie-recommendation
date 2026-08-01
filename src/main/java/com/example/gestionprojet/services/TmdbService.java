package com.example.gestionprojet.services;

import com.example.gestionprojet.dto.ActeurDto;
import com.example.gestionprojet.dto.FilmDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class TmdbService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.url}")
    private String apiUrl;

    // ← WebClient avec 10MB de limite — corrige le DataBufferLimitException
    private WebClient getClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(config -> config
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10 MB
                .build();

        return WebClient.builder()
                .baseUrl(apiUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    private String posterUrl(String path) {
        if (path == null || path.equals("null") || path.isEmpty())
            return null;
        return "https://image.tmdb.org/t/p/w500" + path;
    }

    private FilmDto parseFilm(JsonNode node) {
        FilmDto dto = new FilmDto();
        dto.setTmdbId(node.path("id").asLong());
        dto.setTitre(node.path("title").asText());
        dto.setSynopsis(node.path("overview").asText());
        dto.setNote(node.path("vote_average").asText());
        dto.setDateSortie(node.path("release_date").asText());
        dto.setPosterUrl(posterUrl(
                node.path("poster_path").asText(null)));
        dto.setBackdropUrl(posterUrl(
                node.path("backdrop_path").asText(null)));

        List<String> genres = new ArrayList<>();
        // ⚠️ AVANT : on lisait "genres" (objets {id,name} — présent sur
        // /movie/{id}) PUIS "genre_ids" (ids bruts — présent seulement
        // sur les endpoints de liste comme /trending, /popular). Sur
        // l'endpoint détail, "genre_ids" n'existe pas du tout, donc ce
        // .forEach() itérait sur un nœud vide sans rien casser — mais
        // ça aurait dupliqué les genres en doublon (noms + ids mélangés)
        // si jamais les deux champs avaient existé en même temps.
        // On ne lit "genre_ids" que si "genres" (objet complet) est
        // absent, pour ne jamais mélanger les deux formats.
        JsonNode genresNode = node.path("genres");
        if (genresNode.isArray() && genresNode.size() > 0) {
            genresNode.forEach(g -> genres.add(g.path("name").asText()));
        } else {
            node.path("genre_ids").forEach(g -> genres.add(g.asText()));
        }
        dto.setGenres(genres);

        return dto;
    }

    public List<FilmDto> getTrending() {
        try {
            JsonNode response = getClient().get()
                    .uri("/trending/movie/week?api_key="
                            + apiKey + "&language=fr-FR")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            List<FilmDto> films = new ArrayList<>();
            response.path("results")
                    .forEach(n -> films.add(parseFilm(n)));
            return films;
        } catch (Exception e) {
            System.out.println(">>> getTrending error: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<FilmDto> getPopular() {
        try {
            JsonNode response = getClient().get()
                    .uri("/movie/popular?api_key="
                            + apiKey + "&language=fr-FR")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            List<FilmDto> films = new ArrayList<>();
            response.path("results")
                    .forEach(n -> films.add(parseFilm(n)));
            return films;
        } catch (Exception e) {
            System.out.println(">>> getPopular error: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<FilmDto> getTopRated() {
        try {
            JsonNode response = getClient().get()
                    .uri("/movie/top_rated?api_key="
                            + apiKey + "&language=fr-FR")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            List<FilmDto> films = new ArrayList<>();
            response.path("results")
                    .forEach(n -> films.add(parseFilm(n)));
            return films;
        } catch (Exception e) {
            System.out.println(">>> getTopRated error: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<FilmDto> search(String query) {
        try {
            JsonNode response = getClient().get()
                    .uri("/search/movie?api_key=" + apiKey
                            + "&language=fr-FR&query=" + query)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            List<FilmDto> films = new ArrayList<>();
            response.path("results")
                    .forEach(n -> films.add(parseFilm(n)));
            return films;
        } catch (Exception e) {
            System.out.println(">>> search error: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    public FilmDto getDetail(Long tmdbId) {
        try {
            JsonNode node = getClient().get()
                    .uri("/movie/" + tmdbId + "?api_key="
                            + apiKey + "&language=fr-FR")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            FilmDto dto = parseFilm(node);
            dto.setDuree(node.path("runtime").asText() + " min");

            // ⚠️ AVANT : les acteurs n'étaient JAMAIS récupérés ici.
            // getActeurs(tmdbId) existait déjà comme méthode séparée,
            // mais rien ne l'appelait depuis getDetail(). Le FilmDto
            // repartait avec acteurs=null, donc getOrCreateFilm() côté
            // HistoriqueService n'avait tout simplement rien à
            // sauvegarder — d'où les tables acteurs/film_acteur vides.
            dto.setActeurs(getActeurs(tmdbId));

            return dto;
        } catch (Exception e) {
            System.out.println(">>> getDetail error tmdbId="
                    + tmdbId + ": " + e.getMessage());
            return new FilmDto();
        }
    }

    public List<ActeurDto> getActeurs(Long tmdbId) {
        try {
            JsonNode response = getClient().get()
                    .uri("/movie/" + tmdbId
                            + "/credits?api_key=" + apiKey
                            + "&language=fr-FR")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<ActeurDto> acteurs = new ArrayList<>();
            int count = 0;
            for (JsonNode n : response.path("cast")) {
                if (count++ >= 10) break;
                ActeurDto a = new ActeurDto();
                a.setTmdbId(n.path("id").asLong());
                a.setNom(n.path("name").asText());
                a.setPersonnage(n.path("character").asText());
                a.setPhoto(posterUrl(
                        n.path("profile_path").asText(null)));
                acteurs.add(a);
            }
            return acteurs;
        } catch (Exception e) {
            System.out.println(">>> getActeurs error tmdbId="
                    + tmdbId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String getTrailer(Long tmdbId) {
        try {
            JsonNode response = getClient().get()
                    .uri("/movie/" + tmdbId
                            + "/videos?api_key=" + apiKey
                            + "&language=fr-FR")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            for (JsonNode n : response.path("results")) {
                if ("YouTube".equals(n.path("site").asText())
                        && "Trailer".equals(
                        n.path("type").asText())) {
                    return "https://www.youtube.com/watch?v="
                            + n.path("key").asText();
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println(">>> getTrailer error tmdbId="
                    + tmdbId + ": " + e.getMessage());
            return null;
        }
    }

    public List<FilmDto> getByGenre(int genreId) {
        try {
            JsonNode response = getClient().get()
                    .uri("/discover/movie?api_key=" + apiKey
                            + "&language=fr-FR&with_genres=" + genreId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            List<FilmDto> films = new ArrayList<>();
            response.path("results")
                    .forEach(n -> films.add(parseFilm(n)));
            return films;
        } catch (Exception e) {
            System.out.println(">>> getByGenre error: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }


}