package labs.externalmicroservice.presentation;


import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import labs.SignInRequest;
import labs.SignUpRequest;
import labs.UserDTO;
import labs.externalmicroservice.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;


@RestController
@AllArgsConstructor
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @PostMapping("/create")
    @ApiResponses({
            @ApiResponse(responseCode = "201"),
    })
    public ResponseEntity<UserDTO> signUp(@RequestBody SignUpRequest signUpRequest) {
        if (userService.checkIfUserExists(signUpRequest.username)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        UserDTO user = userService.createUser(signUpRequest);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/signin")
//    @Async("threadPoolTaskExecutor")
//    @Async
//    @Async("abcTaskExecutor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signed in", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "401", description = "Bad credentials", content = { @Content(schema = @Schema(implementation = String.class)) }),
    })
    public CompletableFuture<ResponseEntity<?>> loginUser(@Valid @RequestBody SignInRequest signInRequest) {
        try {
            String jwtCookieString = userService.loginUser(signInRequest);
            return CompletableFuture.completedFuture(
                    ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookieString)
                            .body("Signed in!"));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()));
        }
    }

    @PostMapping("/signout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signed out", content = { @Content(schema = @Schema()) })
    })
    public ResponseEntity<?> logoutUser() {
        String jwtCookieString = userService.logoutUser();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookieString).body("Signed out!");
    }
}
