package projekt.PD.Security;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private long expiresIn;
}
