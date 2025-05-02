package projekt.PD.DataBase.DB_User.User_Service;

import projekt.PD.DataBase.DB_User.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();
    User findUserByLogin(String login);
    User findUserById(int id);
    User createUser(User user);
    User updateUser(User user);
    Boolean deleteUser(int id);
    void changeRole(int id, String role);
    boolean ifUserExists(String login);
    boolean ifUserExists(int id);
    List<User> getUsersByRole(String role);
}
