package com.hrms.backend.controller;

import com.hrms.backend.dto.JwtResponse;
import com.hrms.backend.dto.LoginRequest;
import com.hrms.backend.entity.JwtToken;
import com.hrms.backend.entity.User;
import com.hrms.backend.repository.JwtTokenRepository;
import com.hrms.backend.repository.UserRepository;
import com.hrms.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final JwtTokenRepository tokenRepository;
    private final com.hrms.backend.security.LoginRateLimiter rateLimiter;
    private final com.hrms.backend.service.AuditService auditService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, JwtTokenRepository tokenRepository, com.hrms.backend.security.LoginRateLimiter rateLimiter, com.hrms.backend.service.AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.rateLimiter = rateLimiter;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        if (!rateLimiter.isAllowed(loginRequest.getEmail())) {
            return ResponseEntity.status(429).body("Too many failed login attempts. Please try again in 15 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            rateLimiter.resetAttempts(loginRequest.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication.getName());

            org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            List<String> roles = authorities.stream().filter(a -> a.startsWith("ROLE_")).map(a -> a.substring(5)).collect(Collectors.toList());
            List<String> permissions = authorities.stream().filter(a -> !a.startsWith("ROLE_")).collect(Collectors.toList());

            User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            JwtToken jwtToken = new JwtToken();
            jwtToken.setUser(user);
            jwtToken.setToken(jwt);
            jwtToken.setCreatedAt(LocalDateTime.now());
            jwtToken.setExpiresAt(LocalDateTime.now().plusHours(24));
            tokenRepository.save(jwtToken);

            auditService.log("LOGIN_SUCCESS", loginRequest.getEmail(), "User", user.getUserId().toString(), "User logged in successfully");

            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), roles, permissions));
        } catch (org.springframework.security.core.AuthenticationException e) {
            rateLimiter.recordFailedAttempt(loginRequest.getEmail());
            auditService.log("LOGIN_FAILED", loginRequest.getEmail(), "User", "N/A", "Invalid credentials");
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            tokenRepository.findByToken(jwt).ifPresent(jwtToken -> {
                jwtToken.setLoggedOut(true);
                tokenRepository.save(jwtToken);
                auditService.log("LOGOUT", jwtToken.getUser().getEmail(), "User", jwtToken.getUser().getUserId().toString(), "User logged out");
            });
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        List<String> roles = user.getRoles().stream().map(r -> r.getRoleName()).collect(Collectors.toList());
        List<String> permissions = user.getRoles().stream()
            .flatMap(r -> r.getPermissions().stream())
            .map(p -> p.getPermissionCode())
            .distinct()
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(java.util.Map.of("email", user.getEmail(), "roles", roles, "permissions", permissions));
    }
}
