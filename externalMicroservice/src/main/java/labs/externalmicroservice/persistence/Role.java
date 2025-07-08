package labs.externalmicroservice.persistence;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@NoArgsConstructor
@Getter
@Table(name="Roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private RoleTypes name;

    public Role(final RoleTypes name) {
        this.name = name;
    }
}
