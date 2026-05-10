package com.delivery.pge.service;

import com.delivery.pge.dto.*;
import com.delivery.pge.entity.User;
import com.delivery.pge.exception.DuplicateCpfException;
import com.delivery.pge.exception.DuplicateEmailException;
import com.delivery.pge.repository.UserRepository;
import com.delivery.pge.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Normalize CPF removing formatting characters
        String normalizedCpf = request.getCpf().replaceAll("[.\\-]", "");

        if (userRepository.existsByCpf(normalizedCpf)) {
            throw new DuplicateCpfException();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .cpf(normalizedCpf)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .secondaryPhone(request.getSecondaryPhone())
                .cep(request.getCep())
                .address(request.getAddress())
                .referencePoint(request.getReferencePoint())
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully with ID: {}", saved.getId());
        return mapToUserResponse(saved);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for identifier: {}", request.getIdentifier());

        // Authenticate through Spring Security (uses UserDetailsServiceImpl)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal());

        log.info("Login successful for user: {}", user.getEmail());
        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .cpf(user.getCpf())
                .build();
    }

    private UserResponseDTO mapToUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .cpf(user.getCpf())
                .email(user.getEmail())
                .phone(user.getPhone())
                .secondaryPhone(user.getSecondaryPhone())
                .cep(user.getCep())
                .address(user.getAddress())
                .referencePoint(user.getReferencePoint())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
