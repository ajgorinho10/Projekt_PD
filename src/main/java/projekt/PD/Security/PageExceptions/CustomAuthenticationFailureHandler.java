package projekt.PD.Security.PageExceptions;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;


public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // Pobranie nazwy użytkownika z parametrów żądania HTTP
        String username = request.getParameter("username");

        // Domyślny adres URL przekierowania w przypadku błędu logowania
        String redirectUrl = "/login?error=true";

        /*
         * Sprawdzenie, czy wyjątek związany z błędem logowania zawiera informację o błędzie TOTP.
         * Jeśli wiadomość błędu zawiera słowo "TOTP", oznacza to, że wystąpił problem związany z weryfikacją
         * kodu jednorazowego używanego w dwuskładnikowym uwierzytelnianiu (2FA).
         */
        if (exception.getMessage() != null && exception.getMessage().contains("TOTP")) {
            // Zmiana adresu URL przekierowania na taki, który wskazuje na błąd z TOTP oraz dołączenie nazwy użytkownika.
            redirectUrl = "/login?totpError=true&username=" + username;
        }

        response.sendRedirect(redirectUrl);
    }
}

