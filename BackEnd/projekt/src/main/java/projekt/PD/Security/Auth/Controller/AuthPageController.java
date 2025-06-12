package projekt.PD.Security.Auth.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;
import projekt.PD.Security.Auth.AuthRegister;

@Controller
public class AuthPageController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    public AuthPageController(AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "totpError", required = false) String totpError,
                        @RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "registerMsg", required = false) String registerMsg,
                        Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Incorrect username or password.");
        }

        if (logout != null) {
            model.addAttribute("logoutMsg", "You have been successfully logged out.");
            return "index";
        }

        if (totpError != null) {
            model.addAttribute("errorMsg", "Invalid verification code.");
        }

        if (username != null) {
            model.addAttribute("username", username);
        }

        if(registerMsg != null) {
            model.addAttribute("registerMsg", "Registered Successfully.");
        }

        return "Auth/login";
    }

    @PostMapping("/performLogin")
    public String performLogin(@ModelAttribute String username,
                        @ModelAttribute String password,
                        HttpServletRequest httpRequest,
                        HttpServletResponse httpResponse, Model model) {

        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Zapisanie kontekstu bezpieczeństwa w repozytorium (sesji HTTP)
            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

            // Zapewnienie utworzenia sesji i zapisania w niej kontekstu
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            User user = getUserID();
            model.addAttribute("user", user);
            return "redirect:/User/Information/home";
        }
        catch (Exception e){
            return "Auth/login";
        }

    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("authRegister", new AuthRegister());

        return "Auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute AuthRegister authRegister, Model model, BindingResult result) {
        try{
            boolean error = false;
            if(authRegister.getPassword().length() < 6) {
                result.rejectValue("password", "error.authRegister", "Password must be at least 6 characters.");
                error = true;
            }

            if(authRegister.getLogin().length() < 6) {
                result.rejectValue("login", "error.authRegister", "Login must be at least 6 characters.");
                error = true;
            }

            if(error) {
                return "Auth/register";
            }
            else{
                User savedUser = userService.createUser(authRegister);

                return "redirect:login?registerMsg=true";
            }

        }catch (Exception e){

            if(e.getMessage().contains("Login")) {
                result.rejectValue("login", "error.authRegister", "Login Already Exists.");
            }
            else{
                model.addAttribute("errorMsg", e.getMessage());
            }

            return "Auth/register";
        }
    }

    @GetMapping("/")
    public String index(Model model){
        return "index";
    }


        private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }
}
