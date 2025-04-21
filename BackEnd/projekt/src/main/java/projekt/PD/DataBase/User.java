package projekt.PD.DataBase;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "\"users\"")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Login nie może być pusty")
    private String login;

    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 8 , max= 30,message = "Błędna długość hasła")
    private String password;
}
