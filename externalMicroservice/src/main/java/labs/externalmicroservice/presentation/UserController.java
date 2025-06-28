package labs.externalmicroservice.presentation;


import labs.CreateOwnerDTO;
import labs.SignUpRequest;
import labs.UserDTO;
import labs.externalmicroservice.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @Autowired
    private KafkaTemplate<String, CreateOwnerDTO> createOwnerKafkaTemplate;

    @PostMapping("/create")
    public ResponseEntity<UserDTO> signUp(@RequestBody SignUpRequest signUpRequest) {
        if (userService.checkIfUserExists(signUpRequest.username)) {
            return null;
        }

        UserDTO user = userService.createUser(signUpRequest);

        createOwnerKafkaTemplate.send("create_owner", new CreateOwnerDTO(user.id, signUpRequest.name, signUpRequest.birthday));

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
