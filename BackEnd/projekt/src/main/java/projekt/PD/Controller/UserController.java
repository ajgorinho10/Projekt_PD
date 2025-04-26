package projekt.PD.Controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.User;
import projekt.PD.DataBase.UserRepository;

import java.security.InvalidParameterException;
import java.util.List;

@RestController
@RequestMapping("/users")

public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me1")
    public ResponseMSG<User> getMe1() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return new ResponseMSG<>(HttpStatus.OK.value(),"User info",currentUser);
    }

    @GetMapping("/me2")
    public ResponseMSG<User> getMe2() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return new ResponseMSG<>(HttpStatus.OK.value(),"User info",currentUser);
    }

}
