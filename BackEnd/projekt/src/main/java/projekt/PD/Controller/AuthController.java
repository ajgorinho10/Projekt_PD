package projekt.PD.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projekt.PD.DataBase.User;
import projekt.PD.DataBase.UserRepository;
import projekt.PD.Security.AuthRequest;
import projekt.PD.Security.AuthResponse;
import projekt.PD.Security.AuthenticationService;
import projekt.PD.Security.JwtService;

@RequestMapping("/auth")
@RestController
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthController(UserRepository userRepository, JwtService jwtService, AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseMSG<User> create(@Valid @RequestBody User user) {
        if(user.getLogin().isBlank()) {
            throw new IllegalArgumentException();
        }

        if(userRepository.existsByLogin(user.getLogin())) {
            return new ResponseMSG<>(HttpStatus.FOUND.value(), "Użytkownik o podanym loginie już istnieje", user);
        }

        user.setId(null);
        user.setRoles("USER");
        User registeredUser = authenticationService.signup(user);
        return new ResponseMSG<>(HttpStatus.CREATED.value(), "Użytkownik stworzony", registeredUser);
    }

    @PostMapping("/login")
    public ResponseMSG<AuthResponse> authenticate(@RequestBody AuthRequest input) {
        User authenticatedUser = authenticationService.authenticate(input);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        AuthResponse loginResponse = new AuthResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return new ResponseMSG<>(HttpStatus.OK.value(), "Zalogowano", loginResponse);
    }
}
