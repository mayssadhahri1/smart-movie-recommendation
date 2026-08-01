package com.example.gestionprojet.web;

import com.example.gestionprojet.dto.*;
import com.example.gestionprojet.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "🔐 Authentification", description = "Register, Login JWT et Google Sign-In")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Créer un compte",
            description = "Inscription d'un nouveau client")
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Se connecter",
            description = "Login et obtenir un token JWT")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Se connecter avec Google",
            description = "Login ou création automatique de compte via Google")
    @PostMapping("/google")
    public ResponseEntity<JwtResponse> googleLogin(
            @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }
}