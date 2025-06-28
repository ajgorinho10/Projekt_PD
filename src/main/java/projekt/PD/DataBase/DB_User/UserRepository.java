package projekt.PD.DataBase.DB_User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Interfejs związanych z tabelą users zapytań do bazy danych */

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByLogin(String login);
    boolean existsById(Long id);
    Optional<User> findByLogin(String login);
    Optional<User> findById(Long id);
    void deleteById(Long id);
    List<User> getUsersByRoles(String roles);
}
