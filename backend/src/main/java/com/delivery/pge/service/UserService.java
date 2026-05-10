package com.delivery.pge.service;

import com.delivery.pge.dto.UpdateUserDTO;
import com.delivery.pge.dto.UserResponseDTO;
import com.delivery.pge.entity.User;
import com.delivery.pge.exception.UserNotFoundException;
import com.delivery.pge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO getCurrentUser() {
        User user = getAuthenticatedUser();
        return mapToResponse(user);
    }

    @Transactional
    public UserResponseDTO updateCurrentUser(UpdateUserDTO dto) {
        User user = getAuthenticatedUser();

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getSecondaryPhone() != null) {
            user.setSecondaryPhone(dto.getSecondaryPhone());
        }
        if (dto.getCep() != null && !dto.getCep().isBlank()) {
            user.setCep(dto.getCep());
        }
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getReferencePoint() != null && !dto.getReferencePoint().isBlank()) {
            user.setReferencePoint(dto.getReferencePoint());
        }

        User saved = userRepository.save(user);
        log.info("User {} updated successfully", saved.getId());
        return mapToResponse(saved);
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UserNotFoundException("Usuário não autenticado");
        }
        return (User) auth.getPrincipal();
    }

    private UserResponseDTO mapToResponse(User user) {
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
