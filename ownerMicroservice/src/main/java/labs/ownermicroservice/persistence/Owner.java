package labs.ownermicroservice.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name="Owners")
@NoArgsConstructor
@Getter
public class Owner {
    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owners_seq")
//    @SequenceGenerator(name = "owners_seq", sequenceName = "owners_seq", allocationSize = 1)
    @Setter
    private long id;

    @Column(length=255)
    @Setter
    private String name;

    @Column(name="birth_date")
    private LocalDate birthDate;

    public Owner(long id, String name, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
    }

    public Owner(String name, LocalDate birthDate) {
        this.name = name;
        this.birthDate = birthDate;
    }
}
