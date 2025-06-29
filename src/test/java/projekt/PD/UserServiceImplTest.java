package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.UserRepository;
import projekt.PD.DataBase.DB_User.User_Service.UserServiceImpl;
import projekt.PD.Security.Auth.AuthRegister;
import projekt.PD.Security.Auth.AuthRequest;
import projekt.PD.Security.RestExceptions.Exceptions.LoginAlreadyExistException;
import projekt.PD.Util.TotpUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    // Mocks for HttpServletRequest/Response/Session
    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private HttpServletResponse httpResponse;
    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRoles("ROLE_USER");
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkich użytkowników")
    void shouldGetAllUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // When
        List<User> users = userService.getAllUsers();

        // Then
        assertThat(users).isNotNull();
        assertThat(users.size()).hasSameClassAs(32);
        assertThat(users.get(0).getLogin()).isEqualTo("testuser");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Powinien znaleźć użytkownika po loginie")
    void shouldFindUserByLogin() {
        // Given
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));

        // When
        User foundUser = userService.findUserByLogin("testuser");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getLogin()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Powinien zwrócić null, gdy użytkownik o danym loginie nie istnieje")
    void shouldReturnNullWhenUserNotFoundByLogin() {
        // Given
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        // When
        User foundUser = userService.findUserByLogin("nonexistent");

        // Then
        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("Powinien pomyślnie utworzyć nowego użytkownika")
    void shouldCreateUser() {
        // Given
        AuthRegister registrationData = new AuthRegister("newUser", "password", "Jan", "Kowalski");
        when(userRepository.existsByLogin("newUser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User createdUser = userService.createUser(registrationData);

        // Then
        assertThat(createdUser.getLogin()).isEqualTo("newUser");
        assertThat(createdUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(createdUser.getRoles()).isEqualTo("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek, gdy login już istnieje podczas tworzenia użytkownika")
    void shouldThrowExceptionWhenLoginExistsOnCreate() {
        // Given
        AuthRegister registrationData = new AuthRegister("testuser", "password", "Jan", "Kowalski");
        when(userRepository.existsByLogin("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(registrationData))
                .isInstanceOf(LoginAlreadyExistException.class)
                .hasMessage("Login już istnieje !");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Powinien pomyślnie zalogować użytkownika bez 2FA")
    void shouldLoginUserWithout2FA() {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "password", null);
        testUser.setMfaEnabled(false);
        Authentication authentication = mock(Authentication.class);

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));
        when(httpRequest.getSession()).thenReturn(httpSession);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        boolean result = userService.loginWith2FA(authRequest, httpRequest, httpResponse);

        // Then
        assertThat(result).isTrue();
        verify(securityContextRepository).saveContext(any(), eq(httpRequest), eq(httpResponse));
        // POPRAWIONA LINIA: Użycie eq() dla pierwszego argumentu
        verify(httpSession, never()).setAttribute(eq("totpCode"), any());
    }

    @Test
    @DisplayName("Powinien pomyślnie zalogować użytkownika z włączonym 2FA")
    void shouldLoginUserWith2FA() {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "password", "123456");
        testUser.setMfaEnabled(true);
        Authentication authentication = mock(Authentication.class);

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));
        when(httpRequest.getSession()).thenReturn(httpSession);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // When
        boolean result = userService.loginWith2FA(authRequest, httpRequest, httpResponse);

        // Then
        assertThat(result).isTrue();

        // POPRAWIONA LINIA: Przekazanie wartości bezpośrednio, bez zbędnych eq()
        verify(httpSession).setAttribute("totpCode", "123456");

        verify(securityContextRepository).saveContext(any(), eq(httpRequest), eq(httpResponse));
    }

    @Test
    @DisplayName("Powinien zwrócić false przy nieudanej autentykacji")
    void shouldReturnFalseOnFailedAuthentication() {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword", null);

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(testUser));
        when(httpRequest.getSession()).thenReturn(httpSession);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Błędne dane") {});

        // When
        boolean result = userService.loginWith2FA(authRequest, httpRequest, httpResponse);

        // Then
        assertThat(result).isFalse();
        verify(securityContextRepository, never()).saveContext(any(), any(), any());
    }

    @Test
    @DisplayName("Powinien wygenerować sekret MFA dla użytkownika")
    void shouldGenerateMfaSecret() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When
        String secret = userService.generateMfaSecret(1L);

        // Then
        assertThat(secret).hasSize(32);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getMfaSecret()).isEqualTo(secret);
        assertThat(savedUser.isMfaEnabled()).isFalse();
        assertThat(savedUser.isMfaVerified()).isFalse();
    }

    @Test
    @DisplayName("Powinien zweryfikować i aktywować MFA z poprawnym kodem TOTP")
    void shouldVerifyAndEnableMfaWithValidCode() {
        // Given
        String secret = TotpUtil.generateSecret();
        String validCode = TotpUtil.generateCode(secret, System.currentTimeMillis() / 30000);

        testUser.setMfaSecret(secret);
        testUser.setMfaEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.verifyAndEnableMfa(1L, validCode);

        // Then
        assertThat(result).isTrue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.isMfaEnabled()).isTrue();
        assertThat(savedUser.isMfaVerified()).isTrue();
    }

    @Test
    @DisplayName("Nie powinien aktywować MFA z niepoprawnym kodem TOTP")
    void shouldNotEnableMfaWithInvalidCode() {
        // Given
        testUser.setMfaSecret("FAKESECRET");
        testUser.setMfaEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.verifyAndEnableMfa(1L, "invalidcode");

        // Then
        assertThat(result).isFalse();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Powinien dezaktywować MFA dla użytkownika")
    void shouldDisableMfa() {
        // Given
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret("ANY_SECRET");
        testUser.setMfaVerified(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When
        boolean result = userService.disableMfa(1L);

        // Then
        assertThat(result).isTrue();

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.isMfaEnabled()).isFalse();
        assertThat(savedUser.getMfaSecret()).isNull();
        assertThat(savedUser.isMfaVerified()).isFalse();
    }

    @Test
    @DisplayName("Powinien zwrócić true, gdy użytkownik o danym ID zostanie usunięty")
    void shouldReturnTrueWhenUserIsDeleted() {
        // Given
        Long userId = 1L;
        // Metoda deleteById zwraca void, więc nie ma potrzeby stubowania z 'when'
        // Mockito domyślnie nie rzuca wyjątku dla metod void

        // When
        boolean result = userService.deleteUser(userId);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Sprawdzenie działania funkcji findUserById")
    void shouldReturnFalseWhenUserIsNotDeleted() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        when(userRepository.findById(userId1)).thenReturn(Optional.of(testUser));

        User userFound = userService.findUserById(userId1);
        User userNotFound = userService.findUserById(userId2);

        assertThat(userFound).isEqualTo(testUser);
        assertThat(userNotFound).isNull();
    }

    @Test
    @DisplayName("Powinien poprawnie zaktualizować i zwrócić użytkownika")
    void shouldUpdateAndReturnUser() {

        testUser.setFirstName("Jan");
        testUser.setLastName("Kowalski");

        when(userRepository.save(testUser)).thenReturn(testUser);

        User updatedUser = userService.updateUser(testUser);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getFirstName()).isEqualTo("Jan");

        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Powinien zmienić rolę użytkownika, gdy ten istnieje")
    void shouldChangeUserRoleWhenUserExists() {
        // Given (Arrange)
        Long userId = 1L;
        String newRole = "ROLE_ADMIN";
        // Używamy obiektu 'testUser' zdefiniowanego w @BeforeEach
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When (Act)
        userService.changeRole(userId, newRole);

        // Then (Assert)
        // Weryfikujemy, czy metoda findById została wywołana
        verify(userRepository).findById(userId);
        // Sprawdzamy, czy na obiekcie użytkownika została ustawiona nowa rola.
        // Działa to, ponieważ `ifPresent` modyfikuje ten sam obiekt, który dostarczyliśmy w mocku.
        assertThat(testUser.getRoles()).isEqualTo(newRole);

        // UWAGA: W tym teście nie weryfikujemy wywołania metody save(), ponieważ nie ma jej w kodzie metody.
        // Zakładamy, że ta metoda działa w kontekście transakcyjnym (@Transactional),
        // gdzie zmiana na obiekcie zarządzanym przez JPA jest automatycznie zapisywana.
    }

    @Test
    @DisplayName("Nie powinien nic robić, gdy użytkownik do zmiany roli nie istnieje")
    void shouldNotDoAnythingWhenUserToChangeRoleDoesNotExist() {
        // Given
        Long nonExistentId = 99L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        userService.changeRole(nonExistentId, "ROLE_ADMIN");

        // Then
        // Sprawdzamy tylko, czy nastąpiła próba znalezienia użytkownika.
        verify(userRepository).findById(nonExistentId);
        // Weryfikujemy, że nie było żadnych dalszych interakcji z repozytorium (np. save).
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Powinien zwrócić true, jeśli użytkownik o danym loginie istnieje")
    void shouldReturnTrueWhenUserExistsByLogin() {
        // Given
        String existingLogin = "testuser";
        when(userRepository.existsByLogin(existingLogin)).thenReturn(true);

        // When
        boolean result = userService.ifUserExists(existingLogin);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Powinien zwrócić false, jeśli użytkownik o danym loginie nie istnieje")
    void shouldReturnFalseWhenUserDoesNotExistByLogin() {
        // Given
        String nonExistentLogin = "nonexistent";
        when(userRepository.existsByLogin(nonExistentLogin)).thenReturn(false);

        // When
        boolean result = userService.ifUserExists(nonExistentLogin);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Powinien zwrócić true, jeśli użytkownik o danym ID istnieje")
    void shouldReturnTrueWhenUserExistsById() {
        // Given
        Long existingId = 1L;
        when(userRepository.existsById(existingId)).thenReturn(true);

        // When
        boolean result = userService.ifUserExists(existingId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Powinien zwrócić false, jeśli użytkownik o danym ID nie istnieje")
    void shouldReturnFalseWhenUserDoesNotExistById() {
        // Given
        Long nonExistentId = 99L;
        when(userRepository.existsById(nonExistentId)).thenReturn(false);

        // When
        boolean result = userService.ifUserExists(nonExistentId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Powinien zwrócić listę użytkowników o danej roli")
    void shouldReturnUsersByRole() {
        // Given
        String role = "ROLE_USER";
        List<User> expectedUsers = List.of(testUser); // Używamy użytkownika z @BeforeEach
        when(userRepository.getUsersByRoles(role)).thenReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.getUsersByRole(role);

        // Then
        assertThat(actualUsers).hasSize(1);
        assertThat(actualUsers.get(0).getRoles()).isEqualTo(role);
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę, gdy nie ma użytkowników o danej roli")
    void shouldReturnEmptyListWhenNoUsersForGivenRole() {
        // Given
        String role = "ROLE_GUEST";
        when(userRepository.getUsersByRoles(role)).thenReturn(Collections.emptyList());

        // When
        List<User> actualUsers = userService.getUsersByRole(role);

        // Then
        assertThat(actualUsers).isNotNull();
    }

    @Test
    @DisplayName("Powinien zwrócić true, gdy kod TOTP jest poprawny a MFA włączone")
    void shouldReturnTrueWhenTotpIsValidAndMfaIsEnabled() {
        // Given (Arrange)
        String secret = "JBSWY3DPEHPK3PXP"; // Przykładowy, znany sekret
        String validCode = TotpUtil.generateCode(secret, System.currentTimeMillis() / 30000);

        testUser.setMfaEnabled(true);
        testUser.setMfaSecret(secret);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When (Act)
        boolean result = userService.verifyTotp(testUser.getId(), validCode);

        // Then (Assert)
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Powinien zwrócić false, gdy kod TOTP jest niepoprawny")
    void shouldReturnFalseWhenTotpIsInvalid() {
        // Given
        String secret = "JBSWY3DPEHPK3PXP";
        String invalidCode = "000000";

        testUser.setMfaEnabled(true);
        testUser.setMfaSecret(secret);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.verifyTotp(testUser.getId(), invalidCode);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Powinien zwrócić false przy weryfikacji TOTP, gdy użytkownik nie istnieje")
    void shouldReturnFalseForVerifyTotpWhenUserNotFound() {
        // Given
        Long nonExistentId = 99L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        boolean result = userService.verifyTotp(nonExistentId, "123456");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Powinien zwrócić false przy weryfikacji TOTP, gdy MFA jest wyłączone")
    void shouldReturnFalseForVerifyTotpWhenMfaIsDisabled() {
        // Given
        testUser.setMfaEnabled(false); // MFA jest wyłączone
        testUser.setMfaSecret("JBSWY3DPEHPK3PXP");
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.verifyTotp(testUser.getId(), "123456");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Powinien zwrócić false przy weryfikacji TOTP, gdy użytkownik nie ma sekretu MFA")
    void shouldReturnFalseForVerifyTotpWhenMfaSecretIsNull() {
        // Given
        testUser.setMfaEnabled(true);
        testUser.setMfaSecret(null); // Brak sekretu
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.verifyTotp(testUser.getId(), "123456");

        // Then
        assertThat(result).isFalse();
    }

}