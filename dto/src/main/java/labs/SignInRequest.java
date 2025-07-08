package labs;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SignInRequest {
    public String username;
    public String password;

    public SignInRequest() {}
}
