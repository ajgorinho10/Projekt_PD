package projekt.PD.Security.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Klasa AuthResponse polega na przechowywaniu odpowiedzi z serwera po próbie logowania.
 * Zawiera informacje o powodzeniu operacji, wiadomość oraz token uwierzytelniający.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
}
