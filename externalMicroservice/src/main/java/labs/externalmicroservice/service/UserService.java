package labs.externalmicroservice.service;

import labs.SignInRequest;
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
    long getId(Authentication authentication);
    String loginUser(SignInRequest signInRequest);
    String logoutUser();
}
