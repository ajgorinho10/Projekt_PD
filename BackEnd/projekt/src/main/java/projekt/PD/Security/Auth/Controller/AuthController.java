package projekt.PD.Security.Auth.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.Security.Auth.AuthRegister;
import projekt.PD.Security.Auth.AuthRequest;
import projekt.PD.Security.Auth.AuthResponse;
import projekt.PD.Security.RestExceptions.Exceptions.InvalidLoginOrPasswordException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest input,
                                           HttpServletRequest httpRequest,
                                           HttpServletResponse httpResponse) {
        try{
            // Przeprowadzenie uwierzytelniania
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getLogin(),
                            input.getPassword()
                    )
            );

            // Tworzenie nowego kontekstu bezpieczeństwa
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Zapisanie kontekstu bezpieczeństwa w repozytorium (sesji HTTP)
            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

            // Zapewnienie utworzenia sesji i zapisania w niej kontekstu
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            // Dodajemy informacje o sesji do odpowiedzi
            AuthResponse response = new AuthResponse(
                    true,
                    "Zalogowano pomyślnie: " + authentication.getName(),
                    session.getId()
            );
            return ResponseEntity.ok(response);

        }catch (Exception e){
            throw new InvalidLoginOrPasswordException("Błędny login lub hasło");
        }

    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRegister input){

        User user = new User();
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setLogin(input.getLogin());
        user.setPassword(input.getPassword());
        user.setRoles("ROLE_USER");

        User savedUser = userService.createUser(user);
        return new ResponseEntity<>(new AuthResponse(true, input.getLogin(), input.getPassword()), HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        // Wylogowanie użytkownika
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Wyczyszczenie ciasteczka JSESSIONID
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setPath("/auth/logout");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(new AuthResponse(true, "You have logged out successfully.", null));
    }
}
