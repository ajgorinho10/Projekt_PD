package projekt.PD;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void afterEach() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(users, result);
    }

    @Test
    void findUserByLogin_ShouldReturnUser_WhenExists() {
        User user = new User();
        user.setLogin("test");
        when(userRepository.findByLogin("test")).thenReturn(Optional.of(user));

        User result = userService.findUserByLogin("test");

        assertNotNull(result);
        assertEquals("test", result.getLogin());
    }

    @Test
    void findUserByLogin_ShouldReturnNull_WhenNotExists() {
        when(userRepository.findByLogin("test")).thenReturn(Optional.empty());

        User result = userService.findUserByLogin("test");

        assertNull(result);
    }

    @Test
    void findUserById_ShouldReturnUser_WhenExists() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findUserById_ShouldReturnNull_WhenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        User result = userService.findUserById(1L);

        assertNull(result);
    }

    @Test
    void createUser_ShouldThrowException_WhenLoginExists() {
        AuthRegister input = new AuthRegister();
        input.setLogin("existing");
        input.setPassword("pass");
        input.setFirstName("fn");
        input.setLastName("ln");

        when(userRepository.existsByLogin("existing")).thenReturn(true);

        assertThrows(LoginAlreadyExistException.class, () -> userService.createUser(input));
    }

    @Test
    void createUser_ShouldSaveUser_WhenLoginNotExists() {
        AuthRegister input = new AuthRegister();
        input.setLogin("newlogin");
        input.setPassword("pass");
        input.setFirstName("fn");
        input.setLastName("ln");

        when(userRepository.existsByLogin("newlogin")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        User savedUser = new User();
        savedUser.setLogin("newlogin");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(input);

        assertNotNull(result);
        assertEquals("newlogin", result.getLogin());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldSaveAndReturnUser() {
        User user = new User();
        user.setLogin("user");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(user);

        assertEquals(user, result);
    }

    @Test
    void deleteUser_ShouldDeleteByIdAndReturnTrue() {
        boolean result = userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
        assertTrue(result);
    }

    @Test
    void changeRole_ShouldUpdateUserRole_WhenUserFound() {
        User user = new User();
        user.setRoles("ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.changeRole(1L, "ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", user.getRoles());
    }

    @Test
    void changeRole_ShouldDoNothing_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        userService.changeRole(1L, "ROLE_ADMIN");

        // No exception thrown, no changes made
        verify(userRepository, never()).save(any());
    }

    @Test
    void ifUserExists_Login_ShouldReturnTrue_WhenExists() {
        when(userRepository.existsByLogin("login")).thenReturn(true);

        assertTrue(userService.ifUserExists("login"));
    }

    @Test
    void ifUserExists_Login_ShouldReturnFalse_WhenNotExists() {
        when(userRepository.existsByLogin("login")).thenReturn(false);

        assertFalse(userService.ifUserExists("login"));
    }

    @Test
    void ifUserExists_Id_ShouldReturnTrue_WhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertTrue(userService.ifUserExists(1L));
    }

    @Test
    void ifUserExists_Id_ShouldReturnFalse_WhenNotExists() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertFalse(userService.ifUserExists(1L));
    }

    @Test
    void getUsersByRole_ShouldReturnList() {
        List<User> users = List.of(new User());
        when(userRepository.getUsersByRoles("ROLE_USER")).thenReturn(users);

        List<User> result = userService.getUsersByRole("ROLE_USER");

        assertEquals(users, result);
    }

    // ========== TESTY loginWith2FA ==========

    @Test
    void loginWith2FA_ShouldReturnFalse_WhenUserNotFound() {
        AuthRequest request = new AuthRequest("Login","Password",null);
        request.setLogin("nonexistent");

        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        boolean result = userService.loginWith2FA(request, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        assertFalse(result);
    }

    @Test
    void loginWith2FA_ShouldSetTotpCodeInSession_WhenMfaEnabled() {
        User user = new User();
        user.setMfaEnabled(true);

        AuthRequest request = new AuthRequest("Login","Password",null);
        request.setLogin("user");
        request.setPassword("pass");
        request.setTotpCode("123456");

        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(httpRequest.getSession()).thenReturn(session);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        doNothing().when(securityContextRepository).saveContext(securityContext, httpRequest, httpResponse);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doNothing().when(securityContext).setAuthentication(authentication);

        boolean result = userService.loginWith2FA(request, httpRequest, httpResponse);

        assertTrue(result);
        verify(session).setAttribute("totpCode", "123456");
        verify(session).setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        verify(securityContextRepository).saveContext(securityContext, httpRequest, httpResponse);
    }

    @Test
    void loginWith2FA_ShouldReturnFalse_WhenAuthenticationFails() {
        User user = new User();
        user.setMfaEnabled(false);

        AuthRequest request = new AuthRequest("Login","Password",null);
        request.setLogin("user");
        request.setPassword("wrong");

        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(httpRequest.getSession()).thenReturn(session);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationException("bad credentials") {});

        boolean result = userService.loginWith2FA(request, httpRequest, httpResponse);

        assertFalse(result);
    }

    // ========== TESTY generateMfaSecret ==========

    @Test
    void generateMfaSecret_ShouldReturnNull_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        String secret = userService.generateMfaSecret(1L);

        assertNull(secret);
    }

    @Test
    void generateMfaSecret_ShouldGenerateAndSaveSecret_WhenUserFound() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        try (var mockedStatic = mockStatic(TotpUtil.class)) {
            mockedStatic.when(TotpUtil::generateSecret).thenReturn("secret-code");

            String secret = userService.generateMfaSecret(1L);

            assertEquals("secret-code", secret);
            assertEquals("secret-code", user.getMfaSecret());
        }
    }

    // ========== TESTY verifyAndEnableMfa ==========

    @Test
    void verifyAndEnableMfa_ShouldReturnFalse_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.verifyAndEnableMfa(1L, "123456");

        assertFalse(result);
    }

    @Test
    void verifyAndEnableMfa_ShouldReturnFalse_WhenMfaAlreadyEnabled() {
        User user = new User();
        user.setMfaEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.verifyAndEnableMfa(1L, "123456");

        assertFalse(result);
    }

    @Test
    void verifyAndEnableMfa_ShouldReturnFalse_WhenMfaSecretIsNull() {
        User user = new User();
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.verifyAndEnableMfa(1L, "123456");

        assertFalse(result);
    }

    @Test
    void verifyAndEnableMfa_ShouldEnableMfa_WhenCodeIsValid() {
        User user = new User();
        user.setMfaEnabled(false);
        user.setMfaSecret("secret");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        try (var mockedStatic = mockStatic(TotpUtil.class)) {
            mockedStatic.when(() -> TotpUtil.verifyCode("secret", "123456")).thenReturn(true);

            boolean result = userService.verifyAndEnableMfa(1L, "123456");

            assertTrue(result);
            assertTrue(user.isMfaEnabled());
            assertTrue(user.isMfaVerified());
        }
    }

    @Test
    void verifyAndEnableMfa_ShouldReturnFalse_WhenCodeInvalid() {
        User user = new User();
        user.setMfaEnabled(false);
        user.setMfaSecret("secret");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        try (var mockedStatic = mockStatic(TotpUtil.class)) {
            mockedStatic.when(() -> TotpUtil.verifyCode("secret", "123456")).thenReturn(false);

            boolean result = userService.verifyAndEnableMfa(1L, "123456");

            assertFalse(result);
            verify(userRepository, never()).save(any());
        }
    }

    // ========== TESTY verifyTotp ==========

    @Test
    void verifyTotp_ShouldReturnFalse_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.verifyTotp(1L, "123456");

        assertFalse(result);
    }

    @Test
    void verifyTotp_ShouldReturnFalse_WhenMfaNotEnabled() {
        User user = new User();
        user.setMfaEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.verifyTotp(1L, "123456");

        assertFalse(result);
    }

    @Test
    void verifyTotp_ShouldReturnFalse_WhenMfaSecretNull() {
        User user = new User();
        user.setMfaEnabled(true);
        user.setMfaSecret(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userService.verifyTotp(1L, "123456");

        assertFalse(result);
    }

    @Test
    void verifyTotp_ShouldReturnTrue_WhenCodeValid() {
        User user = new User();
        user.setMfaEnabled(true);
        user.setMfaSecret("secret");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        try (var mockedStatic = mockStatic(TotpUtil.class)) {
            mockedStatic.when(() -> TotpUtil.verifyCode("secret", "123456")).thenReturn(true);

            boolean result = userService.verifyTotp(1L, "123456");

            assertTrue(result);
        }
    }

    // ========== TESTY disableMfa ==========

    @Test
    void disableMfa_ShouldReturnFalse_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.disableMfa(1L);

        assertFalse(result);
    }

    @Test
    void disableMfa_ShouldDisableMfaAndSave_WhenUserFound() {
        User user = new User();
        user.setMfaEnabled(true);
        user.setMfaSecret("secret");
        user.setMfaVerified(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        boolean result = userService.disableMfa(1L);

        assertTrue(result);
        assertFalse(user.isMfaEnabled());
        assertNull(user.getMfaSecret());
        assertFalse(user.isMfaVerified());
        verify(userRepository).save(user);
    }
}
