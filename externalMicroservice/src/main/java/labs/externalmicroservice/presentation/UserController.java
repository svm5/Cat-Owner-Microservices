package labs.externalmicroservice.presentation;


import jakarta.validation.Valid;
import labs.CreateOwnerDTO;
import labs.SignInRequest;
import labs.SignUpRequest;
import labs.UserDTO;
import labs.externalmicroservice.security.jwt.JwtService;
import labs.externalmicroservice.service.UserDetailsImpl;
import labs.externalmicroservice.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;


@RestController
@AllArgsConstructor
public class UserController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    JwtService jwtService;

    @Autowired
    private KafkaTemplate<String, CreateOwnerDTO> createOwnerKafkaTemplate;

    @PostMapping("/create")
    public ResponseEntity<UserDTO> signUp(@RequestBody SignUpRequest signUpRequest) {
        if (userService.checkIfUserExists(signUpRequest.username)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        UserDTO user = userService.createUser(signUpRequest);

        createOwnerKafkaTemplate.send("create_owner", new CreateOwnerDTO(user.id, signUpRequest.name, signUpRequest.birthday));

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
//    @Async("threadPoolTaskExecutor")
//    @Async
//    @Async("abcTaskExecutor")
    public CompletableFuture<ResponseEntity<?>> loginUser(@Valid @RequestBody SignInRequest signInRequest) {
        System.out.println("!!!!! 1");
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.username, signInRequest.password)
            );
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()));
        }

        System.out.println("!!!!! 2");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("!!!!! 3");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        System.out.println("!!!!! 4");
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(userDetails);
        System.out.println("!!!!! 5 " + HttpHeaders.SET_COOKIE + " " + jwtCookie.toString());
        return CompletableFuture.completedFuture(ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body("Signed in!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie jwtCookie = jwtService.getCleanJwtCookie();
        SecurityContextHolder.getContext().setAuthentication(null);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body("Signed out!");
    }
}
