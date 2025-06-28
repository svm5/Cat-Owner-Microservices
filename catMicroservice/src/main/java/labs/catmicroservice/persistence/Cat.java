package labs.catmicroservice.persistence;

import jakarta.persistence.*;
import labs.CatColors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Cats")
@Getter
@NoArgsConstructor
public class Cat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cats_seq")
    @SequenceGenerator(name = "cats_seq", sequenceName = "cats_seq", allocationSize = 1)
    private long id;

    @Column(length=255)
    private String name;

    @Column(name="birth_date")
    private LocalDate birthDate;

    @Column(length=255, name="breed_name")
    private String breedName;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private CatColors color;

    @Column(name = "eyes_color", length=255)
    private String eyesColor;

    @Setter
    private long owner_id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="cats_friendship",
        joinColumns = @JoinColumn(name="first_cat_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name="second_cat_id", referencedColumnName = "id"))
    private List<Cat> friends = new ArrayList<>();

    public Cat(long id, String name, LocalDate birthDate, String breedName, CatColors color, String eyesColor, long owner_id) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.breedName = breedName;
        this.color = color;
        this.eyesColor = eyesColor;
        this.owner_id = owner_id;
    }

    public Cat(String name, LocalDate birthDate, String breedName, CatColors color, String eyesColor, long owner_id) {
        this.name = name;
        this.birthDate = birthDate;
        this.breedName = breedName;
        this.color = color;
        this.eyesColor = eyesColor;
        this.owner_id = owner_id;
    }

    public boolean addFriend(Cat cat) {
        if (cat.getId() == this.getId())
            return false;

        return friends.add(cat);
    }

    public boolean removeFriend(Cat cat) {
        if (!friends.contains(cat)) {
            return false;
        }

        return friends.remove(cat);
    }
}
