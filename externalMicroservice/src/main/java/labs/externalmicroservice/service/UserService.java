package labs.externalmicroservice.service;

import labs.SignUpRequest;
import labs.UserDTO;
import org.springframework.security.core.Authentication;

public interface UserService {
    boolean checkIfUserExists(String username);
    UserDTO createUser(SignUpRequest signUpRequest);
    boolean checkUserAuthenticated(long id);
    boolean checkUserAuthenticated(long id, Authentication authentication);
    boolean checkAdmin();
    boolean checkAdmin(Authentication authentication);
}
