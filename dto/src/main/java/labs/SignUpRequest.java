package labs;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
public class SignUpRequest {
    public String username;
    public String password;
    public String name;
    public LocalDate birthday;

    public SignUpRequest() { }
}
