package com.example.InvestmentGoalManagementPlatform.service;

import com.example.InvestmentGoalManagementPlatform.DTO.AuthResponse;
import com.example.InvestmentGoalManagementPlatform.DTO.ForgotPasswordRequest;
import com.example.InvestmentGoalManagementPlatform.DTO.ForgotPasswordResponse;
import com.example.InvestmentGoalManagementPlatform.DTO.LoginRequest;
import com.example.InvestmentGoalManagementPlatform.DTO.RegisterRequest;
import com.example.InvestmentGoalManagementPlatform.DTO.ResetPasswordRequest;
import com.example.InvestmentGoalManagementPlatform.entity.User;
import com.example.InvestmentGoalManagementPlatform.repository.UserRepository;
import com.example.InvestmentGoalManagementPlatform.utility.Role;
import com.example.InvestmentGoalManagementPlatform.security.GoogleTokenVerifier;
import com.example.InvestmentGoalManagementPlatform.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long RESET_TOKEN_VALIDITY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hash it!
        user.setRole(Role.USER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse( user.getId() ,user.getFullName(), token,user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        // Throws AuthenticationException (BadCredentialsException, etc.) on failure —
        // let it propagate; the controller/exception handler decides the HTTP response.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail());
        if(user == null){
            throw new UsernameNotFoundException(
                    "User not found with email: " + request.getEmail()
            );
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse( user.getId() ,user.getFullName(), token,user.getEmail(), user.getRole().name());
    }

    /**
     * Signs a user in from a Google ID token, creating the account on first use.
     */
    public AuthResponse loginWithGoogle(String idToken) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

        String email = payload.getEmail();
        User user = userRepository.findByEmail(email);

        if (user == null) {
            user = createGoogleUser(email, (String) payload.get("name"));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse( user.getId() ,user.getFullName(), token,user.getEmail(), user.getRole().name());
    }

    private User createGoogleUser(String email, String name) {
        // findByEmail only matches active rows, so a deactivated account with this
        // address would otherwise collide with the unique constraint on insert.
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "This account has been deactivated. Please contact support."
            );
        }

        User user = new User();
        user.setFullName(name != null && !name.isBlank() ? name : email);
        user.setEmail(email);

        // Google users never sign in with a password, but CustomUserDetailsService
        // rejects a null one — store an unguessable value they can never present.
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            // Don't leak whether the address is registered — same generic response either way.
            return new ForgotPasswordResponse(
                    "If that email exists, a reset link has been generated.",
                    null,
                    null
            );
        }

        String token = UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(
                LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES)
        );

        userRepository.save(user);

        // Dev mode: no mail sender is wired up, so the link is returned directly
        // instead of emailed.
        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        return new ForgotPasswordResponse(
                "Reset link generated (dev mode — no email sent).",
                token,
                resetLink
        );
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userRepository.findByResetPasswordToken(request.getToken());

        if (user == null
                || user.getResetPasswordTokenExpiry() == null
                || user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {

            if (user != null) {
                user.setResetPasswordToken(null);
                user.setResetPasswordTokenExpiry(null);
                userRepository.save(user);
            }

            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);

        userRepository.save(user);
    }
}