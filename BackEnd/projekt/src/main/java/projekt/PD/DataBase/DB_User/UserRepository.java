package projekt.PD.DataBase.DB_User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByLogin(String login);
    boolean existsById(int id);
    User findByLogin(String login);
    User findById(int id);
    void deleteById(int id);
    List<User> getUsersByRoles(String roles);
}
