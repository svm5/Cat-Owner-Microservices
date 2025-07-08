package labs.ownermicroservice.service;

import labs.CreateOwnerDTO;
import labs.OwnerDTO;
import labs.OwnerNotFoundException;
import labs.ownermicroservice.persistence.Owner;
import labs.OwnerCriteriaDTO;
import labs.ownermicroservice.persistence.OwnerRepository;
import labs.ownermicroservice.persistence.OwnerSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OwnerServiceImpl implements OwnerService {
    @Autowired
    private OwnerRepository ownerRepository;

    public OwnerDTO createOwner(CreateOwnerDTO ownerDTO) {
        Owner owner = OwnerDTOMapping.fromCreateDto(ownerDTO);
        owner.setId(ownerDTO.id);
        Owner savedOwner = ownerRepository.save(owner);
        return OwnerDTOMapping.toDTO(savedOwner);
    }

    public OwnerDTO getOwner(long ownerId) {
        return OwnerDTOMapping.toDTO(ownerRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException("Owner with id " + ownerId + " not found")));
//        System.out.println("Searching owner with id: " + ownerId);
//        Optional<Owner> owner = ownerRepository.findById(ownerId);
//        System.out.println("Found owner" + owner);
//        System.out.println("Found owner" + owner.isEmpty());
//        if (owner.isEmpty()) {
//            return null;
//        }
//
//        System.out.println("Found owner" + OwnerDTOMapping.toDTO(owner.get()));
//        return OwnerDTOMapping.toDTO(owner.get());
    }

    public Page<OwnerDTO> getAllOwners(OwnerCriteriaDTO ownerCriteriaDto, int page, int size) {
        Specification<Owner> ownerSpecification = OwnerSpecification.getSpecification(ownerCriteriaDto);
        PageRequest pageRequest = PageRequest.of(page, size);
        return ownerRepository.findAll(ownerSpecification, pageRequest).map(OwnerDTOMapping::toDTO);
    }

    public void deleteOwner(long ownerId) {
        ownerRepository.deleteById(ownerId);
    }

    public void deleteAllOwners() {
        ownerRepository.deleteAll();
    }
}
