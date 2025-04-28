package projekt.PD.DataBase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByLogin(String login);
    User findByLogin(String login);
    User findById(int id);
    void deleteById(int id);
}
