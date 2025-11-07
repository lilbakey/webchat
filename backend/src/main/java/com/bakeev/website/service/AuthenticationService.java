package com.bakeev.website.service;

import com.bakeev.website.entity.EmailVerificationToken;
import com.bakeev.website.entity.User;
import com.bakeev.website.repository.EmailVerificationTokenRepository;
import com.bakeev.website.jwt.JwtAuthenticationResponse;
import com.bakeev.website.utils.Role;
import com.bakeev.website.utils.SignInRequest;
import com.bakeev.website.utils.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .real(request.getPassword())
                .role(Role.ROLE_USER)
                .lastSeen(LocalDateTime.now())
                .build();

        userService.create(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(249999999));
        tokenRepository.save(verificationToken);

        String verificationLink = "http://localhost:8080/auth/verify?token=" + token;
//        String verificationLink = "http://192.168.0.107:8080/auth/verify?token=" + token;
        emailService.sendEmail(user.getEmail()
                , "Подтвердите вашу электронную почту"
                , "Нажмите на ссылку, чтобы подтвердить \n" + verificationLink);

        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        UserDetails user = userService.userDetailsService().loadUserByUsername(request.getUsername());

        if (!((User) user).isEmailVerified()) {
            System.err.println("error!");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email не подтвержден!");
        }

        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }

}
