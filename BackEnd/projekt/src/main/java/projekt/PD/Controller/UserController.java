package projekt.PD.Controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import projekt.PD.DataBase.DB_User.User;
import projekt.PD.DataBase.DB_User.User_Service.UserService;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(Model model) {
        User user = getUserID();
        model.addAttribute("user", user);

        return "home";
    }

    private User getUserID() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByLogin(auth.getName());
    }
}
