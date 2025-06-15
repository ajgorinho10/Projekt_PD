package projekt.PD.Security.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * Klasa AuthRequest służy do przechowywania danych logowania użytkownika.
 * @NotBlank - pole nie może być puste
 */

@Data
public class AuthRequest {

    @NotBlank
    private String login;
    @NotBlank
    private String password;
}
