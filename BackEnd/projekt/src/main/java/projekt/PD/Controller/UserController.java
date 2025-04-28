package projekt.PD.Controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.User;
import projekt.PD.DataBase.UserRepository;
import projekt.PD.Security.Service.UserService;

import java.security.InvalidParameterException;
import java.util.List;

@RestController
@RequestMapping("/users")

public class UserController {

    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userService = userService;
    }

    @RolesAllowed("ADMIN")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByLogin(auth.getName());

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping
    public User newUser(@RequestBody User user) {
        user.setRoles("ROLE_USER");
        return userService.createUser(user);
    }
}
