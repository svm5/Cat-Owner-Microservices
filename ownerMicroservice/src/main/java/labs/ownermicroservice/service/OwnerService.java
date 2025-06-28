package labs.ownermicroservice.service;


import labs.CreateOwnerDTO;
import labs.OwnerDTO;
import labs.OwnerCriteriaDTO;
import org.springframework.data.domain.Page;

public interface OwnerService {
    OwnerDTO createOwner(CreateOwnerDTO ownerDTO);
    OwnerDTO getOwner(long ownerId);
    Page<OwnerDTO> getAllOwners(OwnerCriteriaDTO ownerCriteriaDto, int page, int size);
    void deleteOwner(long ownerId);
    void deleteAllOwners();
}
