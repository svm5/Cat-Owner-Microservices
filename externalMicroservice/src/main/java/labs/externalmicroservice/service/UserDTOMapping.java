package labs.externalmicroservice.service;

import labs.UserDTO;
import labs.externalmicroservice.persistence.User;

public class UserDTOMapping {
    public static UserDTO toDTO(final User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername());

//                user.getOwner().getName(),
//                user.getOwner().getBirthDate(),
//                user.getOwner().getCats().stream().map(Cat::getId).toList());
    }
}
