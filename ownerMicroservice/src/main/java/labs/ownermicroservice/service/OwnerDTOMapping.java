package labs.ownermicroservice.service;


import labs.CreateOwnerDTO;
import labs.OwnerDTO;
import labs.ownermicroservice.persistence.Owner;

import java.util.ArrayList;

public class OwnerDTOMapping {
    public static OwnerDTO toDTO(final Owner owner) {
        return new OwnerDTO(
                owner.getId(),
                owner.getName(),
                owner.getBirthDate(),
                new ArrayList<>()
        );
    }

    public static Owner fromCreateDto(final CreateOwnerDTO dto) {
        return new Owner(dto.id, dto.name, dto.birthday);
    }
}
