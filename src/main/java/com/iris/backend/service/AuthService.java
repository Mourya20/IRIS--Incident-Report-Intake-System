package com.iris.backend.service;

import com.iris.backend.dto.AuthDtos;
import com.iris.backend.entity.AppUser;
import com.iris.backend.enums.RoleName;
import com.iris.backend.repository.AppUserRepository;
import com.iris.backend.repository.RoleRepository;
import com.iris.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        var reporterRole = roleRepository.findByName(RoleName.REPORTER)
                .orElseThrow(() -> new IllegalArgumentException("REPORTER role not configured"));

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(reporterRole);

        AppUser saved = appUserRepository.save(user);
        return new AuthDtos.AuthResponse(jwtService.generateToken(saved));
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        return new AuthDtos.AuthResponse(jwtService.generateToken(user));
    }
}
