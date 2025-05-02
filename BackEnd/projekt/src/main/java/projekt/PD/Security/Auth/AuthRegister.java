package projekt.PD.Security.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegister {

    private String login;
    private String password;
    private String firstName;
    private String lastName;
}
