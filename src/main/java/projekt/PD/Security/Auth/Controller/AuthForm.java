package projekt.PD.Security.Auth.Controller;

import lombok.Getter;
import lombok.Setter;

/**
 * Klasa AuthForm służy do przechowywania danych logowania użytkownika.
 * Zawiera pola login i password, które są używane do autoryzacji.
 * Potencjalnie do usunięcia
 */

@Setter
@Getter
public class AuthForm {
    private String login;
    private String password;
    private String TotpCode;
}
