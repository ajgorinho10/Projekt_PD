package projekt.PD.DataBase.DB_User.User_Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.Security.Auth.AuthRegister;
import projekt.PD.Security.Auth.AuthRequest;

import java.util.List;

/** Interfejs metod związanych z zarządzaniem użytkownikami */

public interface UserService {

    List<User> getAllUsers();
    User findUserByLogin(String login);
    User findUserById(Long id);
    User createUser(AuthRegister user);
    User updateUser(User user);
    Boolean deleteUser(Long id);
    void changeRole(Long id, String role);
    boolean ifUserExists(String login);
    boolean ifUserExists(Long id);
    List<User> getUsersByRole(String role);

    boolean loginWith2FA(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
    String generateMfaSecret(Long userId);
    boolean verifyAndEnableMfa(Long userId, String totpCode);
    boolean verifyTotp(Long userId, String totpCode);
    boolean disableMfa(Long userId);
}
