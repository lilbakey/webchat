package com.bakeev.website.controller;


import com.bakeev.website.entity.EmailVerificationToken;
import com.bakeev.website.entity.User;
import com.bakeev.website.repository.EmailVerificationTokenRepository;
import com.bakeev.website.service.AuthenticationService;
import com.bakeev.website.jwt.JwtAuthenticationResponse;
import com.bakeev.website.utils.SignInRequest;
import com.bakeev.website.utils.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final EmailVerificationTokenRepository tokenRepository;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/signup")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/signin")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        Optional<EmailVerificationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty())
            return ResponseEntity.badRequest().body("Неверный верификационный токен");

        EmailVerificationToken verificationToken = optionalToken.get();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now()))
            return ResponseEntity.badRequest().body("Токен истек!");

        User user = verificationToken.getUser();
        user.setEmailVerified(true);

        tokenRepository.delete(verificationToken);

        return ResponseEntity.ok("Электронная почта успешно подтверждена!");

    }
}
