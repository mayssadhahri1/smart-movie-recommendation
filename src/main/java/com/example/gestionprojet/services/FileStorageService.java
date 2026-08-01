package com.example.gestionprojet.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> EXTENSIONS_AUTORISEES =
            List.of(".mp4", ".mkv", ".webm", ".mov");

    public String sauvegarderVideo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vidéo vide ou manquant");
        }

        String nomOriginal = file.getOriginalFilename();
        String extension = nomOriginal != null && nomOriginal.contains(".")
                ? nomOriginal.substring(nomOriginal.lastIndexOf(".")).toLowerCase()
                : "";

        if (!EXTENSIONS_AUTORISEES.contains(extension)) {
            throw new IllegalArgumentException(
                    "Format non supporté. Formats autorisés : " + EXTENSIONS_AUTORISEES);
        }

        Path dossier = Paths.get(uploadDir);
        if (!Files.exists(dossier)) {
            Files.createDirectories(dossier);
        }

        String nomFichier = UUID.randomUUID() + extension;
        Path cible = dossier.resolve(nomFichier);

        Files.copy(file.getInputStream(), cible, StandardCopyOption.REPLACE_EXISTING);

        log.info("✅ Vidéo sauvegardée sur le disque : {}", nomFichier);
        return nomFichier;
    }

    public void supprimerVideo(String nomFichier) {
        if (nomFichier == null) return;
        try {
            Path chemin = Paths.get(uploadDir).resolve(nomFichier);
            Files.deleteIfExists(chemin);
            log.info("🗑️ Vidéo supprimée : {}", nomFichier);
        } catch (IOException e) {
            log.warn("⚠️ Impossible de supprimer la vidéo {} : {}", nomFichier, e.getMessage());
        }
    }

    // ── Méthode ajoutée pour VideoController ─────────────────────────────
    public Path getVideoPath(String nomFichier) {
        return Paths.get(uploadDir).resolve(nomFichier).normalize();
    }
}