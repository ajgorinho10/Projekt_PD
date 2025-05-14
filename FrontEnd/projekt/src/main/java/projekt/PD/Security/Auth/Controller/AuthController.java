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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.Security.Auth.AuthRegister;
import projekt.PD.Security.Auth.AuthRequest;
import projekt.PD.Security.Auth.AuthResponse;
import projekt.PD.Security.RestExceptions.Exceptions.InvalidLoginOrPasswordException;

//@RestController
@Controller
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

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }



    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("formData", new AuthRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("formData") AuthRequest input,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse,
                        RedirectAttributes redirectAttributes,
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
            return "redirect:/auth/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Błędny login lub hasło");
            model.addAttribute("formData", new AuthForm()); // to reset form
            return "login"; // return to login page with error
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        return "dashboard";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("formData", new AuthRegister());
        return "register";
    }



    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("formData") AuthRegister input,
                           Model model) {
        try {
            User user = new User();
            user.setFirstName(input.getFirstName());
            user.setLastName(input.getLastName());
            user.setLogin(input.getLogin());
            user.setPassword(input.getPassword());
            user.setRoles("ROLE_USER");

            userService.createUser(user);

            model.addAttribute("message", "Rejestracja zakończona sukcesem! Możesz się teraz zalogować.");
            return "login"; // a new success page you'll create

        } catch (Exception e) {
            model.addAttribute("error", "Rejestracja nie powiodła się: " + e.getMessage());
            return "register";
        }
    }


    @PostMapping("/logout")
    public String logoutHtml(HttpServletRequest request, HttpServletResponse response, Model model) {
        // Clear security context
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear session cookie
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        model.addAttribute("message", "Zostałeś poprawnie wylogowany.");
        return "index";
    }

}
