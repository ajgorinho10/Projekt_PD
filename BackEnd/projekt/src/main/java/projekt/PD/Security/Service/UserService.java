package projekt.PD.Security.Service;

import projekt.PD.DataBase.User;

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
}
