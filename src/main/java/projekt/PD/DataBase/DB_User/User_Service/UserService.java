package projekt.PD.DataBase.DB_User.User_Service;

import projekt.PD.DataBase.DB_User.User;
import projekt.PD.Security.Auth.AuthRegister;

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
}
