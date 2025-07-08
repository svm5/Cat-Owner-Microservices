package labs.ownermicroservice;

import jakarta.persistence.EntityNotFoundException;
import labs.CreateOwnerDTO;
import labs.OwnerCriteriaDTO;
import labs.OwnerDTO;
import labs.ownermicroservice.persistence.Owner;
import labs.ownermicroservice.persistence.OwnerRepository;
import labs.ownermicroservice.service.OwnerServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwnerServiceTests {
    private LocalDate birthday = LocalDate.of(2000, 1, 10);;
    private Owner owner = new Owner(1, "Sveta", birthday);;

    @Mock
    OwnerRepository ownerRepository;

    @InjectMocks
    OwnerServiceImpl ownerService;

    @Test
    public void createOwnerTest() {
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);
        CreateOwnerDTO createOwnerDTO = new CreateOwnerDTO(5, "Sveta", birthday);

        OwnerDTO ownerDTO = ownerService.createOwner(createOwnerDTO);

        Mockito.verify(ownerRepository, Mockito.times(1)).save(any(Owner.class));
        Assertions.assertEquals(createOwnerDTO.name, ownerDTO.name);
        Assertions.assertEquals(createOwnerDTO.birthday, ownerDTO.birthDate);
    }

    @Test
    public void getOwnerSuccessTest() {
        when(ownerRepository.findById(any(Long.class))).thenReturn(Optional.of(owner));

        OwnerDTO ownerDTO = ownerService.getOwner(owner.getId());

        Mockito.verify(ownerRepository, Mockito.times(1)).findById(any(Long.class));
        Assertions.assertEquals(owner.getName(), ownerDTO.name);
        Assertions.assertEquals(owner.getBirthDate(), ownerDTO.birthDate);
    }

    @Test
    public void getOwnerFailureTest() {
        when(ownerRepository.findById(any(Long.class))).thenThrow(new EntityNotFoundException());

        Assertions.assertThrows(EntityNotFoundException.class, () -> ownerService.getOwner(5));
        verify(ownerRepository, Mockito.times(1)).findById(any(Long.class));
    }

    @Test
    public void getAllOwnersTest() {
        Owner second = new Owner(2, "Alisa", LocalDate.of(2003, 3, 9));
        Owner third = new Owner(3, "Alex", LocalDate.of(2002, 10, 3));

        OwnerCriteriaDTO ownerCriteriaDTO = new OwnerCriteriaDTO(null, null);
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Owner> owners = new PageImpl<>(List.of(owner, second, third), pageRequest, 3);
        when(ownerRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(owners);

        Page<OwnerDTO> ownerDTOs = ownerService.getAllOwners(ownerCriteriaDTO, 0, 2);

        Mockito.verify(ownerRepository, Mockito.times(1)).findAll(any(Specification.class), any(Pageable.class));
        Assertions.assertEquals(3, ownerDTOs.stream().toList().size());
        List<String> names = new ArrayList<>();
        List<LocalDate> birthDates = new ArrayList<>();
        for (OwnerDTO ownerDTO : ownerDTOs) {
            names.add(ownerDTO.name);
            birthDates.add(ownerDTO.birthDate);
        }
        Assertions.assertEquals(names, owners.stream().map(Owner::getName).collect(Collectors.toList()));
        Assertions.assertEquals(birthDates, owners.stream().map(Owner::getBirthDate).collect(Collectors.toList()));
    }

    @Test
    public void deleteOwnerSuccessTest() {
        ownerService.deleteOwner(owner.getId());

        Mockito.verify(ownerRepository, Mockito.times(1)).deleteById(owner.getId());
    }

    @Test
    public void deleteOwnerFailureTest() {
        doThrow(EntityNotFoundException.class).when(ownerRepository).deleteById(owner.getId());

        Assertions.assertThrows(EntityNotFoundException.class, () -> ownerService.deleteOwner(owner.getId()));
        Assertions.assertDoesNotThrow(() -> ownerService.deleteOwner(owner.getId() + 2));
    }

    @Test
    public void deleteAllOwnersTest() {
        ownerService.deleteAllOwners();

        Mockito.verify(ownerRepository, Mockito.times(1)).deleteAll();
    }
}