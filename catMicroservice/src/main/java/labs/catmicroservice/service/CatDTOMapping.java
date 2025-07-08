package labs.catmicroservice.service;

import labs.CatDTO;
import labs.CreateCatDTO;
import labs.catmicroservice.persistence.Cat;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CatDTOMapping {
    public static CatDTO toDTO(final Cat cat) {
        return new CatDTO(
                cat.getId(),
                cat.getName(),
                cat.getBirthDate(),
                cat.getBreedName(),
                cat.getColor(),
                cat.getOwner_id(),
                cat.getEyesColor(),
                cat.getFriends().stream().map(Cat::getId).collect(Collectors.toList()));
    }

    public Cat fromCreateDTO(final CreateCatDTO catDTO) {
        return new Cat(catDTO.name, catDTO.birthday, catDTO.breed, catDTO.color, catDTO.eyesColor, catDTO.owner_id);
    }
}
