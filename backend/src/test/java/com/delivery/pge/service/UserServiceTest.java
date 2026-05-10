package com.delivery.pge.service;

import com.delivery.pge.dto.UpdateUserDTO;
import com.delivery.pge.dto.UserResponseDTO;
import com.delivery.pge.entity.User;
import com.delivery.pge.exception.UserNotFoundException;
import com.delivery.pge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ─── Helper: sets up the SecurityContext with a mock user ───────

    private User setupAuthenticatedUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("Carlos Pereira")
                .cpf("11122233344")
                .email("carlos@email.com")
                .phone("11988887777")
                .secondaryPhone("11977776666")
                .cep("04538-133")
                .address("Av. Brigadeiro Faria Lima, 3477, São Paulo - SP")
                .referencePoint("Próximo ao Shopping Iguatemi")
                .createdAt(LocalDateTime.now())
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        return user;
    }

    // ─── getCurrentUser() ──────────────────────────────────────────

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        @Test
        @DisplayName("Deve retornar UserResponseDTO com dados do usuário autenticado")
        void shouldReturnCurrentUserData() {
            User user = setupAuthenticatedUser();

            UserResponseDTO result = userService.getCurrentUser();

            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("Carlos Pereira");
            assertThat(result.getEmail()).isEqualTo("carlos@email.com");
            assertThat(result.getCpf()).isEqualTo("11122233344");
        }

        @Test
        @DisplayName("Deve mapear todos os campos do usuário corretamente")
        void shouldMapAllFields() {
            User user = setupAuthenticatedUser();

            UserResponseDTO result = userService.getCurrentUser();

            assertThat(result.getId()).isEqualTo(user.getId());
            assertThat(result.getPhone()).isEqualTo("11988887777");
            assertThat(result.getSecondaryPhone()).isEqualTo("11977776666");
            assertThat(result.getCep()).isEqualTo("04538-133");
            assertThat(result.getAddress()).isEqualTo("Av. Brigadeiro Faria Lima, 3477, São Paulo - SP");
            assertThat(result.getReferencePoint()).isEqualTo("Próximo ao Shopping Iguatemi");
        }

        @Test
        @DisplayName("Deve lançar UserNotFoundException quando não há autenticação")
        void shouldThrowWhenNotAuthenticated() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.isAuthenticated()).thenReturn(false);

            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar UserNotFoundException quando contexto de segurança está nulo")
        void shouldThrowWhenSecurityContextIsNull() {
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    // ─── updateCurrentUser() ───────────────────────────────────────

    @Nested
    @DisplayName("updateCurrentUser()")
    class UpdateCurrentUser {

        @Test
        @DisplayName("Deve atualizar telefone principal quando informado")
        void shouldUpdatePhone() {
            User user = setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setPhone("11966665555");

            UserResponseDTO result = userService.updateCurrentUser(dto);

            assertThat(result.getPhone()).isEqualTo("11966665555");
        }

        @Test
        @DisplayName("Deve atualizar endereço quando informado")
        void shouldUpdateAddress() {
            User user = setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setAddress("Rua Haddock Lobo, 595, São Paulo - SP");

            UserResponseDTO result = userService.updateCurrentUser(dto);

            assertThat(result.getAddress()).isEqualTo("Rua Haddock Lobo, 595, São Paulo - SP");
        }

        @Test
        @DisplayName("Deve atualizar o CEP quando informado")
        void shouldUpdateCep() {
            User user = setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setCep("01310-100");

            UserResponseDTO result = userService.updateCurrentUser(dto);

            assertThat(result.getCep()).isEqualTo("01310-100");
        }

        @Test
        @DisplayName("Deve atualizar o local de referência quando informado")
        void shouldUpdateReferencePoint() {
            User user = setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setReferencePoint("Ao lado do banco");

            UserResponseDTO result = userService.updateCurrentUser(dto);

            assertThat(result.getReferencePoint()).isEqualTo("Ao lado do banco");
        }

        @Test
        @DisplayName("Não deve alterar campos quando o DTO envia valores nulos")
        void shouldNotUpdateFieldsWhenDtoIsNull() {
            User user = setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO(); // todos os campos nulos

            UserResponseDTO result = userService.updateCurrentUser(dto);

            // Valores originais devem permanecer
            assertThat(result.getPhone()).isEqualTo("11988887777");
            assertThat(result.getCep()).isEqualTo("04538-133");
        }

        @Test
        @DisplayName("Não deve alterar campos quando o DTO envia strings em branco")
        void shouldNotUpdateFieldsWhenDtoIsBlank() {
            User user = setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setPhone("   "); // branco — não deve ser aplicado
            dto.setAddress("   ");

            UserResponseDTO result = userService.updateCurrentUser(dto);

            assertThat(result.getPhone()).isEqualTo("11988887777");
            assertThat(result.getAddress()).isEqualTo("Av. Brigadeiro Faria Lima, 3477, São Paulo - SP");
        }

        @Test
        @DisplayName("Deve chamar userRepository.save() exatamente uma vez")
        void shouldSaveUserOnce() {
            setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setPhone("11955554444");

            userService.updateCurrentUser(dto);

            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Deve salvar o usuário com o telefone secundário atualizado")
        void shouldUpdateSecondaryPhone() {
            setupAuthenticatedUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserDTO dto = new UpdateUserDTO();
            dto.setSecondaryPhone("11944443333");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            userService.updateCurrentUser(dto);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getSecondaryPhone()).isEqualTo("11944443333");
        }
    }
}
