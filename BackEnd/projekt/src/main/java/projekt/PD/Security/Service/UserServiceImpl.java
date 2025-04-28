package projekt.PD.Security.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import projekt.PD.DataBase.User;
import projekt.PD.DataBase.UserRepository;
import projekt.PD.Security.RestExceptions.Exceptions.LoginAlreadyExistException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public User findUserById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(User user) {

        if(userRepository.findByLogin(user.getLogin()) != null) {
            throw new LoginAlreadyExistException("Login już istnieje !");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Boolean deleteUser(int id) {
       userRepository.deleteById(id);

       return true;
    }

    @Override
    public void changeRole(int id, String role) {
        User user = userRepository.findById(id);
        user.setRoles(role);
    }

    @Override
    public boolean ifUserExists(String login) {
        return userRepository.existsByLogin(login);
    }
}
