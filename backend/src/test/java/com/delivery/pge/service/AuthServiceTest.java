package com.delivery.pge.service;

import com.delivery.pge.dto.RegisterRequestDTO;
import com.delivery.pge.dto.LoginRequestDTO;
import com.delivery.pge.dto.LoginResponseDTO;
import com.delivery.pge.dto.UserResponseDTO;
import com.delivery.pge.entity.User;
import com.delivery.pge.exception.DuplicateCpfException;
import com.delivery.pge.exception.DuplicateEmailException;
import com.delivery.pge.repository.UserRepository;
import com.delivery.pge.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthService — Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ─── Fixtures ───────────────────────────────────────────────────

    private RegisterRequestDTO validRegisterRequest() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setFullName("João Silva");
        dto.setCpf("123.456.789-09");
        dto.setEmail("joao@email.com");
        dto.setPassword("Senha@123");
        dto.setPhone("11999999999");
        dto.setCep("01310-100");
        dto.setAddress("Av. Paulista, 1000, São Paulo - SP");
        dto.setReferencePoint("Próximo ao MASP");
        return dto;
    }

    private User savedUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .fullName("João Silva")
                .cpf("12345678909")
                .email("joao@email.com")
                .password("encoded_password")
                .phone("11999999999")
                .cep("01310100")
                .address("Av. Paulista, 1000, São Paulo - SP")
                .referencePoint("Próximo ao MASP")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── Register ───────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @BeforeEach
        void setUp() {
            when(userRepository.existsByCpf(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser());
        }

        @Test
        @DisplayName("Deve registrar usuário com dados válidos e retornar UserResponseDTO")
        void shouldRegisterValidUser() {
            UserResponseDTO result = authService.register(validRegisterRequest());

            assertThat(result).isNotNull();
            assertThat(result.getFullName()).isEqualTo("João Silva");
            assertThat(result.getEmail()).isEqualTo("joao@email.com");
            assertThat(result.getCpf()).isEqualTo("12345678909");
        }

        @Test
        @DisplayName("Deve normalizar CPF removendo pontos e traços antes de salvar")
        void shouldNormalizeCpfBeforeSaving() {
            authService.register(validRegisterRequest());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getCpf()).doesNotContain(".", "-");
            assertThat(captor.getValue().getCpf()).hasSize(11);
        }

        @Test
        @DisplayName("Deve codificar a senha antes de salvar")
        void shouldEncodePassword() {
            authService.register(validRegisterRequest());

            verify(passwordEncoder).encode("Senha@123");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("encoded_password");
        }

        @Test
        @DisplayName("Deve salvar o usuário no repositório exatamente uma vez")
        void shouldSaveUserOnce() {
            authService.register(validRegisterRequest());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar DuplicateCpfException se CPF já estiver cadastrado")
        void shouldThrowDuplicateCpfException() {
            when(userRepository.existsByCpf("12345678909")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRegisterRequest()))
                    .isInstanceOf(DuplicateCpfException.class);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar DuplicateEmailException se email já estiver cadastrado")
        void shouldThrowDuplicateEmailException() {
            when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRegisterRequest()))
                    .isInstanceOf(DuplicateEmailException.class);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Deve verificar CPF antes do email (CPF tem precedência)")
        void shouldCheckCpfDuplicationBeforeEmail() {
            when(userRepository.existsByCpf(anyString())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRegisterRequest()))
                    .isInstanceOf(DuplicateCpfException.class);

            verify(userRepository, never()).existsByEmail(anyString());
        }
    }

    // ─── Login ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        private User authenticatedUser;
        private Authentication mockAuthentication;

        @BeforeEach
        void setUp() {
            authenticatedUser = savedUser();
            mockAuthentication = mock(Authentication.class);
            when(mockAuthentication.getPrincipal()).thenReturn(authenticatedUser);
            when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);
            when(jwtTokenProvider.generateToken(any())).thenReturn("mocked_jwt_token");
        }

        @Test
        @DisplayName("Deve retornar LoginResponseDTO com token JWT válido")
        void shouldReturnLoginResponseWithToken() {
            LoginRequestDTO req = new LoginRequestDTO();
            req.setIdentifier("joao@email.com");
            req.setPassword("Senha@123");

            LoginResponseDTO result = authService.login(req);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("mocked_jwt_token");
        }

        @Test
        @DisplayName("Deve retornar dados do usuário no LoginResponseDTO")
        void shouldReturnUserDataInResponse() {
            LoginRequestDTO req = new LoginRequestDTO();
            req.setIdentifier("joao@email.com");
            req.setPassword("Senha@123");

            LoginResponseDTO result = authService.login(req);

            assertThat(result.getFullName()).isEqualTo("João Silva");
            assertThat(result.getEmail()).isEqualTo("joao@email.com");
            assertThat(result.getCpf()).isEqualTo("12345678909");
        }

        @Test
        @DisplayName("Deve passar identifier e senha para o AuthenticationManager")
        void shouldPassCredentialsToAuthManager() {
            LoginRequestDTO req = new LoginRequestDTO();
            req.setIdentifier("joao@email.com");
            req.setPassword("Senha@123");

            authService.login(req);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(captor.capture());

            assertThat(captor.getValue().getPrincipal()).isEqualTo("joao@email.com");
            assertThat(captor.getValue().getCredentials()).isEqualTo("Senha@123");
        }

        @Test
        @DisplayName("Deve lançar BadCredentialsException se credenciais forem inválidas")
        void shouldThrowBadCredentialsForInvalidPassword() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Credenciais inválidas"));

            LoginRequestDTO req = new LoginRequestDTO();
            req.setIdentifier("joao@email.com");
            req.setPassword("senha_errada");

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Deve gerar token JWT para o usuário autenticado")
        void shouldGenerateJwtToken() {
            LoginRequestDTO req = new LoginRequestDTO();
            req.setIdentifier("joao@email.com");
            req.setPassword("Senha@123");

            authService.login(req);

            verify(jwtTokenProvider, times(1)).generateToken(authenticatedUser);
        }
    }
}
