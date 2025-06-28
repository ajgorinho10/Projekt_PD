package projekt.PD.Security.PageExceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import projekt.PD.Controller.User_Controller.UserController;
import projekt.PD.Security.Auth.Controller.AuthPageController;

/*
 * Klasa GlobalPageExceptionHandler jest odpowiedzialna za obsługę wyjątków
 * Dodaje informacje o statusie, błędzie i ścieżce, w której wystąpił błąd
 */

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
