package com.example.gestionprojet.services;

import com.example.gestionprojet.dto.*;
import com.example.gestionprojet.entity.*;
import com.example.gestionprojet.repository.UserRepository;
import com.example.gestionprojet.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final String GOOGLE_CLIENT_ID =
            "148399677683-s0bhdfn0ignjr7iss821jn8vlsjn0ge2.apps.googleusercontent.com";

    public JwtResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        User user = User.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .motPasse(passwordEncoder.encode(request.getMotPasse()))
                .role(RoleEnum.ROLE_CLIENT)
                .actif(true)
                .dateInscription(LocalDate.now())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());
        return new JwtResponse(
                token, user.getRole().name(), user.getNom(), user.getId());
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Email introuvable"));

        if (user.getActif() == null || !user.getActif()) {
            throw new RuntimeException("Compte désactivé");
        }

        if (!passwordEncoder.matches(
                request.getMotPasse(), user.getMotPasse())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        user.setDerniereConnexion(LocalDate.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());
        return new JwtResponse(
                token, user.getRole().name(), user.getNom(), user.getId());
    }

    public JwtResponse loginWithGoogle(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new RuntimeException("Token Google invalide");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String nom = (String) payload.get("name");

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .nom(nom != null ? nom : email)
                        .email(email)
                        .motPasse(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(RoleEnum.ROLE_CLIENT)
                        .actif(true)
                        .dateInscription(LocalDate.now())
                        .build();
                return userRepository.save(newUser);
            });

            // Corrige le champ actif si null (cas d'anciens comptes)
            if (user.getActif() == null) {
                user.setActif(true);
            }

            if (!user.getActif()) {
                throw new RuntimeException("Compte désactivé");
            }

            user.setDerniereConnexion(LocalDate.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            return new JwtResponse(
                    token, user.getRole().name(), user.getNom(), user.getId());

        } catch (Exception e) {
            throw new RuntimeException("Erreur authentification Google : " + e.getMessage());
        }
    }
}