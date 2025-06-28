package labs.externalmicroservice.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Table(name="users", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class User {
    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_generator")
//    @SequenceGenerator(name = "users_generator", sequenceName = "users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_seq", allocationSize = 1)
    private long id;

    @Column(length=100)
    private String username;

    @Column(length=100)
    private String password;

    @Setter
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name="user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
