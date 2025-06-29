package projekt.PD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import projekt.PD.Controller.User_Controller.UserController;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.Services.CurrentUser;
import projekt.PD.Util.TotpUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setLogin("testUser");
        mockUser.setMfaEnabled(false);
        mockUser.setMfaSecret("secret");

        when(currentUser.getUserID()).thenReturn(mockUser);
    }

    // ---------------------------
    // /me
    // ---------------------------
    @Test
    void getCurrentUser_ShouldReturnAboutMePage() {
        String view = userController.getCurrentUser(model);

        verify(currentUser).addUserToModel(model);
        assertEquals("User/Information/about-me", view);
    }

    // ---------------------------
    // /home
    // ---------------------------
    @Test
    void home_ShouldReturnHomePageWithUser() {
        String view = userController.home(model);

        verify(model).addAttribute("user", mockUser);
        assertEquals("User/Information/home", view);
    }

    // ---------------------------
    // GET /totp-setup when MFA is disabled
    // ---------------------------
    @Test
    void showTotpSetupPage_ShouldDisplayQRCode_WhenMfaDisabled() {
        when(userService.generateMfaSecret(mockUser.getId())).thenReturn("newSecret");
        mockUser.setMfaEnabled(false); // MFA off

        String uri = "mockUri";
        String qr = "mockQr";
        mockStatic(TotpUtil.class);
        when(TotpUtil.generateTotpUri(anyString(), anyString(), anyString())).thenReturn(uri);
        when(TotpUtil.generateQrCode(uri)).thenReturn(qr);

        String view = userController.showTotpSetupPage(authentication, model, null);

        verify(model).addAttribute("qrCodeImage", qr);
        verify(model).addAttribute("mfaSecret", "newSecret");
        assertEquals("Totp/totp-setup", view);
    }

    // ---------------------------
    // GET /totp-setup when MFA is enabled
    // ---------------------------
    @Test
    void showTotpSetupPage_ShouldShowMessage_WhenMfaEnabled() {
        mockUser.setMfaEnabled(true); // MFA on

        String view = userController.showTotpSetupPage(authentication, model, "success");

        verify(model).addAttribute("msg", "Pomyślnie aktywowano Totp");
        assertEquals("Totp/totp-setup", view);
    }

    // ---------------------------
    // POST /totp-setup success
    // ---------------------------
    @Test
    void processTotpSetup_ShouldRedirectToSuccess_WhenCodeValid() {
        when(userService.verifyAndEnableMfa(mockUser.getId(), "123456")).thenReturn(true);

        String view = userController.processTotpSetup(authentication, "123456", model);

        assertEquals("redirect:/totp-setup?status=success", view);
    }

    // ---------------------------
    // POST /totp-setup error
    // ---------------------------
    @Test
    void processTotpSetup_ShouldRedirectToError_WhenCodeInvalid() {
        when(userService.verifyAndEnableMfa(mockUser.getId(), "invalid")).thenReturn(false);

        String view = userController.processTotpSetup(authentication, "invalid", model);

        assertEquals("redirect:/totp-setup?status=error", view);
    }

    // ---------------------------
    // POST /totp-disable success
    // ---------------------------
    @Test
    void disableTotp_ShouldRedirectToTotpSetup_WhenSuccess() {
        when(userService.disableMfa(mockUser.getId())).thenReturn(true);

        String view = userController.disableTotp(authentication);

        assertEquals("redirect:/totp-setup", view);
    }

    // ---------------------------
    // POST /totp-disable failure
    // ---------------------------
    @Test
    void disableTotp_ShouldRedirectToHome_WhenFailure() {
        when(userService.disableMfa(mockUser.getId())).thenReturn(false);

        String view = userController.disableTotp(authentication);

        assertEquals("redirect:/home", view);
    }
}
