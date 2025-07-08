package labs;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
public final class UserDTO {
    public long id;
    public String username;
//    public String name;
//    public LocalDate birthDate;
//    public List<Long> cats;

    public UserDTO() {}
}
