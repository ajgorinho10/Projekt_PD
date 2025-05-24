package projekt.PD.Security.PageExceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import projekt.PD.Controller.UserController;
import projekt.PD.Security.Auth.Controller.AuthPageController;

@ControllerAdvice(assignableTypes = {AuthPageController.class, UserController.class})
public class GlobalPageExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 500);
        model.addAttribute("error", "Błąd serwera");
        model.addAttribute("path", "nieznany");
        return "error";
    }
}
