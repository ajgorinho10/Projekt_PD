package projekt.PD;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.UserRepository;
import projekt.PD.DataBase.DB_User.User_Service.UserServiceImpl;
import projekt.PD.Security.Auth.AuthRegister;
import projekt.PD.Security.Auth.AuthRequest;
import projekt.PD.Security.RestExceptions.Exceptions.LoginAlreadyExistException;
import projekt.PD.Util.TotpUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SecurityContextRepository securityContextRepository;

    @InjectMocks // Tworzy instancję UserServiceImpl i wstrzykuje powyższe mocki
    private UserServiceImpl userService;

    // Mocki dla metod wymagających obiektów Servlet API
    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private HttpServletResponse httpResponse;
    @Mock
    private HttpSession httpSession;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @Test
    @DisplayName("Powinien stworzyć użytkownika, gdy login jest dostępny")
    void shouldCreateUserSuccessfully() {
        // Given
        AuthRegister request = new AuthRegister("nowy_user", "password123", "Jan", "Kowalski");
        when(userRepository.existsByLogin("nowy_user")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("zakodowane_haslo");

        // Symulacja zapisania użytkownika
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User createdUser = userService.createUser(request);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getLogin()).isEqualTo("nowy_user");
        assertThat(createdUser.getPassword()).isEqualTo("zakodowane_haslo");
        assertThat(createdUser.getRoles()).isEqualTo("ROLE_USER");

        verify(userRepository).existsByLogin("nowy_user");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek, gdy login już istnieje")
    void shouldThrowExceptionWhenLoginExists() {
        // Given
        AuthRegister request = new AuthRegister("istniejacy_user", "password123", "Jan", "Kowalski");
        when(userRepository.existsByLogin("istniejacy_user")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(LoginAlreadyExistException.class)
                .hasMessage("Login już istnieje !");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Powinien pomyślnie zalogować użytkownika bez 2FA")
    void shouldLoginSuccessfullyWithout2FA() {
        try (MockedStatic<SecurityContextHolder> mockedContextHolder = mockStatic(SecurityContextHolder.class)) {
            AuthRequest authRequest = new AuthRequest("user", "pass", null);
            User user = new User();
            user.setMfaEnabled(false);

            when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
            when(httpRequest.getSession()).thenReturn(httpSession);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            mockedContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            boolean result = userService.loginWith2FA(authRequest, httpRequest, httpResponse);

            assertThat(result).isTrue();

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(securityContext).setAuthentication(authentication);
            verify(securityContextRepository).saveContext(securityContext, httpRequest, httpResponse);
            verify(httpSession).setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        }
    }

    @Test
    @DisplayName("Powinien zwrócić false przy nieudanej autentykacji")
    void shouldReturnFalseOnAuthenticationFailure() {
        // Given
        AuthRequest authRequest = new AuthRequest("user", "wrong_pass", null);
        User user = new User();
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new AuthenticationException("Błędne dane") {});

        // When
        boolean result = userService.loginWith2FA(authRequest, httpRequest, httpResponse);

        // Then
        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Powinien wygenerować i zapisać sekret MFA dla użytkownika")
    void shouldGenerateAndSaveMfaSecret() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        String secret = userService.generateMfaSecret(userId);

        // Then
        assertThat(secret).isNotNull().hasSize(32); // Standardowa długość sekretu Base32 (20 bajtów)

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getMfaSecret()).isEqualTo(secret);
        assertThat(savedUser.isMfaEnabled()).isFalse();
        assertThat(savedUser.isMfaVerified()).isFalse();
    }


    @Test
    @DisplayName("Powinien pomyślnie zweryfikować i włączyć MFA dla poprawnego kodu TOTP")
    void shouldVerifyAndEnableMfaForValidCode() {
        // Używamy Mockito.mockStatic do zaślepienia statycznej metody TotpUtil.verifyCode
        try (MockedStatic<TotpUtil> mockedTotp = mockStatic(TotpUtil.class)) {
            // Given
            Long userId = 1L;
            String userSecret = "MOJ_SEKRET_BASE32";
            String validTotp = "123456";

            User user = new User();
            user.setId(userId);
            user.setMfaEnabled(false); // MFA jest wyłączone przed weryfikacją
            user.setMfaSecret(userSecret);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            // Mówimy mockowi, że kod jest poprawny
            mockedTotp.when(() -> TotpUtil.verifyCode(userSecret, validTotp)).thenReturn(true);

            // When
            boolean result = userService.verifyAndEnableMfa(userId, validTotp);

            // Then
            assertThat(result).isTrue();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.isMfaEnabled()).isTrue();
            assertThat(savedUser.isMfaVerified()).isTrue();
        }
    }

    @Test
    @DisplayName("Nie powinien włączać MFA dla niepoprawnego kodu TOTP")
    void shouldNotEnableMfaForInvalidCode() {
        try (MockedStatic<TotpUtil> mockedTotp = mockStatic(TotpUtil.class)) {
            // Given
            Long userId = 1L;
            String userSecret = "MOJ_SEKRET_BASE32";
            String invalidTotp = "654321";

            User user = new User();
            user.setId(userId);
            user.setMfaEnabled(false);
            user.setMfaSecret(userSecret);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            // Mówimy mockowi, że kod jest NIEpoprawny
            mockedTotp.when(() -> TotpUtil.verifyCode(anyString(), anyString())).thenReturn(false);

            // When
            boolean result = userService.verifyAndEnableMfa(userId, invalidTotp);

            // Then
            assertThat(result).isFalse();
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    @DisplayName("Powinien pomyślnie wyłączyć MFA dla użytkownika")
    void shouldDisableMfaSuccessfully() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setMfaEnabled(true);
        user.setMfaSecret("JAKIS_SEKRET");
        user.setMfaVerified(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        boolean result = userService.disableMfa(userId);

        // Then
        assertThat(result).isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isMfaEnabled()).isFalse();
        assertThat(savedUser.isMfaVerified()).isFalse();
        assertThat(savedUser.getMfaSecret()).isNull();
    }
}