package com.example.gestionprojet.services;

import com.example.gestionprojet.dto.ProfilDto;
import com.example.gestionprojet.entity.User;
import com.example.gestionprojet.repository.UserFilmRepository;
import com.example.gestionprojet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilService {

    private final UserRepository userRepository;
    private final UserFilmRepository userFilmRepository;
    private final RecommandationService recommandationService;

    @Transactional(readOnly = true)
    public ProfilDto getProfil(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int totalFavoris = userFilmRepository
                .findByUserIdAndFavorisTrue(user.getId()).size();

        int totalConsultations = userFilmRepository
                .findByUserIdOrderByDateConsultationDesc(user.getId()).size();

        ProfilDto dto = new ProfilDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setDateInscription(user.getDateInscription());
        dto.setDerniereConnexion(user.getDerniereConnexion());
        dto.setTotalFavoris(totalFavoris);
        dto.setTotalConsultations(totalConsultations);
        dto.setGenresPreferes(
                recommandationService.getGenresPreferes(email));

        return dto;
    }
}