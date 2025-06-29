package labs.externalmicroservice.service;


import labs.SignUpRequest;
import labs.UserDTO;
import labs.externalmicroservice.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

//    @Autowired
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean checkIfUserExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    @Transactional
    public UserDTO createUser(SignUpRequest signUpRequest) {
        if (this.checkIfUserExists(signUpRequest.username)) {
            throw new IllegalArgumentException("Username is already in use: " + signUpRequest.username);
        }

//        Owner owner = new Owner(signUpRequest.name, signUpRequest.birthday);
        User user = new User(signUpRequest.username, encoder.encode(signUpRequest.password));
//        user.setOwner(owner);
//        owner.setUser(user);
        User savedUser = userRepository.save(user);

        return UserDTOMapping.toDTO(savedUser);
    }

    public boolean checkUserAuthenticated(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("()Auth" + authentication);
//        return authentication != null;
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        return user.getId() == id;
    }

    public boolean checkUserAuthenticated(long id, Authentication authentication) {
//        return authentication != null;
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        return user.getId() == id;
    }

//    public boolean checkAuthority(long id) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null) {
//            return false;
//        }
//
//        String username = authentication.getName();
//        User user = userRepository.findByUsername(username);
//        return user.getId() == id;
//    }
//
//    public boolean checkAuthority(long id, Authentication authentication) {
//        String username = authentication.getName();
//        User user = userRepository.findByUsername(username);
//        return user.getId() == id;
//    }

    public boolean checkAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        for (Role role : user.getRoles()) {
            System.out.println("Role: " + role.getName());
        }

        System.out.println(user.getRoles().stream().map(a -> a.getName()).toList().contains(RoleTypes.ROLE_ADMIN));

//        return user.getRoles().contains(RoleTypes.ROLE_ADMIN);
        return user.getRoles().stream().map(a -> a.getName()).toList().contains(RoleTypes.ROLE_ADMIN);
    }

    public boolean checkAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        for (Role role : user.getRoles()) {
            System.out.println("Role: " + role.getName());
        }

        System.out.println(user.getRoles().stream().map(a -> a.getName()).toList().contains(RoleTypes.ROLE_ADMIN));

//        return user.getRoles().contains(RoleTypes.ROLE_ADMIN);
        return user.getRoles().stream().map(a -> a.getName()).toList().contains(RoleTypes.ROLE_ADMIN);
    }
}
