package labs.externalmicroservice.service;

import labs.GetAllOwnersRequest;
import labs.GetAllOwnersResponse;
import labs.GetOwnerDTO;

public interface OwnerService {
    GetOwnerDTO getOwner(Long id);
    GetAllOwnersResponse getAllOwners(GetAllOwnersRequest request);
    void deleteOwnerById(Long id);
    void deleteAllOwners();
}
