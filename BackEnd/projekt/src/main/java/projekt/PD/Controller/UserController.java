package projekt.PD.Controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.User;
import projekt.PD.DataBase.UserRepository;

import java.security.InvalidParameterException;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseMSG<List<User>> getAll() {
        var users = userRepository.findAll();
        System.out.println(users);
        return new ResponseMSG<>(HttpStatus.OK.value(),"Lista użytkowników",users);
    }

    @PostMapping
    public ResponseMSG<User> create(@Valid @RequestBody User user) {
        if(user.getLogin().isBlank()) {
            throw new IllegalArgumentException();
        }

        if(userRepository.existsByLogin(user.getLogin())) {
            return new ResponseMSG<>(HttpStatus.FOUND.value(), "Użytkownik o podanym loginie już istnieje", user);
        }

        user.setId(null);
        userRepository.saveAndFlush(user);
        return new ResponseMSG<>(HttpStatus.CREATED.value(), "Użytkownik stworzony", user);
    }
}
