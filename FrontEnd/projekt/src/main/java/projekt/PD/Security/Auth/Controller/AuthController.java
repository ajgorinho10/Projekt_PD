package projekt.PD.Security.Auth.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.Security.Auth.AuthRegister;
import projekt.PD.Security.Auth.AuthRequest;
import projekt.PD.Security.Auth.AuthResponse;
import projekt.PD.Security.RestExceptions.Exceptions.InvalidLoginOrPasswordException;

import java.io.IOException;


// to show html
//@RestController
@Controller
// yep
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

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("formData", new AuthForm());
        return "login";
    }


    @PostMapping("/login")
    public String login(@ModelAttribute("formData") AuthForm input,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse,
                        Model model) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getLogin(),
                            input.getPassword()
                    )
            );

            // Set up security context
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

            // Ensure session and attach context
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            // Pass success message to view
            model.addAttribute("message", "Zalogowano pomyślnie jako: " + authentication.getName());
            return "temp-site"; // a new Thymeleaf template you'll create

        } catch (Exception e) {
            model.addAttribute("error", "Błędny login lub hasło");
            model.addAttribute("formData", new AuthForm()); // to reset form
            return "login"; // return to login page with error
        }
    }


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegister input){

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
