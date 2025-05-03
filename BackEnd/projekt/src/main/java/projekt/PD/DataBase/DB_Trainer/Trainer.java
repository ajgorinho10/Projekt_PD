package projekt.PD.DataBase.DB_Trainer;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import projekt.PD.DataBase.DB_User.User;



@Data
@Entity
@Table(name = "\"trainers\"")
@AllArgsConstructor
@NoArgsConstructor
public class Trainer {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    private String specialization;

}
