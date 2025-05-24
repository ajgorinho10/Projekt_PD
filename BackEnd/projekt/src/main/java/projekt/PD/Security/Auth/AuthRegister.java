package projekt.PD.Security.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegister {

    @NotBlank
    @Size(min = 6, max = 30)
    private String login;
    @NotBlank
    @Size(min = 6, max = 30)
    private String password;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
