package labs.catmicroservice;

import jakarta.persistence.EntityNotFoundException;
import labs.*;
import labs.catmicroservice.persistence.Cat;
import labs.catmicroservice.persistence.CatRepository;
import labs.catmicroservice.service.CatDTOMapping;
import labs.catmicroservice.service.CatServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = { CatServiceImpl.class })
public class CatServiceTests {
    @Autowired
    CatServiceImpl catService;

    @MockitoBean
    CatRepository catRepository;

    @MockitoBean
    CatDTOMapping catDTOMapping;

    private final Cat testCat;
    private final LocalDate testOwnerDate = LocalDate.of(1980, 3, 18);
    private final LocalDate testCatDate = LocalDate.of(2015, 10, 13);
    private long owner_id = 1;

    public CatServiceTests() {
        testCat = new Cat("Barsik", testCatDate, "Persian", CatColors.WHITE, "blue", 1);
    }

    @BeforeEach
    void setUp() {
        System.out.println("SetUp");
    }

    @Test
    public void createCatTest() {
        when(catRepository.save(any(Cat.class))).thenReturn(testCat);
        when(catDTOMapping.fromCreateDTO(any(CreateCatDTO.class))).thenReturn(testCat);
        Long owner_id = Long.valueOf(1);
        CreateCatDTO createCatDTO = new CreateCatDTO("Barsik", testCatDate, "Persian", CatColors.WHITE, "green", owner_id);

        CatDTO catDTO = catService.createCat(createCatDTO);

        Mockito.verify(catRepository, Mockito.times(1)).save(any(Cat.class));
        Assertions.assertEquals(testCat.getName(), catDTO.name);
        Assertions.assertEquals(testCat.getBirthDate(), catDTO.birthDate);
        Assertions.assertEquals(testCat.getBreedName(), catDTO.breedName);
        Assertions.assertEquals(testCat.getColor(), catDTO.color);
        Assertions.assertEquals(1, testCat.getOwner_id());
        Assertions.assertEquals(0, catDTO.friends.size());
    }

    @Test
    public void getCatSuccessTest() {
        when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));

        CatDTO catDTO = catService.getCat(testCat.getId());

        Mockito.verify(catRepository, Mockito.times(1)).findById(testCat.getId());
        Assertions.assertEquals(testCat.getName(), catDTO.name);
    }

    @Test
    public void getCatFailureTest() {
        when(catRepository.findById(testCat.getId())).thenReturn(Optional.empty());

        Assertions.assertThrows(CatNotFoundException.class, () -> catService.getCat(testCat.getId()));
        verify(catRepository, Mockito.times(1)).findById(testCat.getId());
    }

    @Test
    public void getAllCatsByOneCriteriaTest() {
        Cat secondTestCat = new Cat("Kometa", testCatDate, "Sfinks", CatColors.BLACK, "green", owner_id);
        Cat thirdTestCat = new Cat("Rizhik", testCatDate, "Siamskaya", CatColors.RED, "yellow", owner_id);
        Cat forthTestCat = new Cat("Abc", testCatDate, "Simple", CatColors.BLACK, "blue", owner_id);

        List<Cat> cats = List.of(secondTestCat, forthTestCat);
        CatCriteriaDTO catCriteriaDTO = new CatCriteriaDTO(null, null, null, CatColors.BLACK, null);
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Cat> catPage = new PageImpl<>(cats, pageRequest, 5);
        when(catRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(catPage);

        Page<CatDTO> catDTOS = catService.getCats(catCriteriaDTO, 0, 2);

        Mockito.verify(catRepository, Mockito.times(1)).findAll(any(Specification.class), any(Pageable.class));
        Assertions.assertEquals(2, catDTOS.stream().count());
        List<String> names = new ArrayList<>();
        for (CatDTO catDTO : catDTOS) {
            names.add(catDTO.name);
        }
        Assertions.assertEquals(names, cats.stream().map(Cat::getName).collect(Collectors.toList()));
    }

    @Test
    public void getAllCatsByMultipleCriteriaTest() {
        Cat forthTestCat = new Cat("Abc", testCatDate, "Simple", CatColors.BLACK, "blue", owner_id);
        Cat fifthTestCat = new Cat("Abc", testCatDate, "Persian", CatColors.BLACK, "green", owner_id);

        List<Cat> cats = List.of(forthTestCat, fifthTestCat);
        CatCriteriaDTO catCriteriaDTO = new CatCriteriaDTO("Abc", null, null, CatColors.BLACK, null);
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Cat> catPage = new PageImpl<>(cats, pageRequest, 5);
        when(catRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(catPage);

        Page<CatDTO> catDTOS = catService.getCats(catCriteriaDTO, 0, 2);

        Mockito.verify(catRepository, Mockito.times(1)).findAll(any(Specification.class), any(Pageable.class));
        Assertions.assertEquals(2, catDTOS.stream().count());
        List<String> names = new ArrayList<>();
        for (CatDTO catDTO : catDTOS) {
            names.add(catDTO.name);
        }
        Assertions.assertEquals(names, cats.stream().map(Cat::getName).collect(Collectors.toList()));
    }

    @Test
    public void changeOwnerTest() {
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));

        long new_owner_id = 2;
        catService.changeOwner(testCat.getId(), new_owner_id);

        Mockito.verify(catRepository, Mockito.times(1)).findById(testCat.getId());
        Mockito.verify(catRepository, Mockito.times(1)).save(testCat);
    }

    @Test
    public void deleteCatSuccessTest() {
        catService.deleteCat(testCat.getId());

        Mockito.verify(catRepository, Mockito.times(1)).deleteById(testCat.getId());
    }

    @Test
    public void deleteCatFailureTest() {
        doThrow(EntityNotFoundException.class).when(catRepository).deleteById(testCat.getId());

        Assertions.assertThrows(EntityNotFoundException.class, () -> catService.deleteCat(testCat.getId()));
        verify(catRepository, Mockito.times(1)).deleteById(testCat.getId());
    }

    @Test
    public void deleteCatsTest() {
        catService.deleteCats();

        Mockito.verify(catRepository, Mockito.times(1)).deleteAll();
    }

    @Test
    public void makeCatsFriendsSuccessTest() {
        Cat secondTestCat = new Cat(10, "Kometa", testCatDate, "Sfinks", CatColors.RED, "green", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));

        catService.makeFriends(testCat.getId(), secondTestCat.getId());

        Mockito.verify(catRepository, Mockito.times(1)).findById(testCat.getId());
        Mockito.verify(catRepository, Mockito.times(1)).findById(secondTestCat.getId());
        Mockito.verify(catRepository, Mockito.times(1)).save(testCat);
        Mockito.verify(catRepository, Mockito.times(1)).save(secondTestCat);
        Assertions.assertTrue(catService.isFriends(testCat.getId(), secondTestCat.getId()));
    }

    @Test
    public void makeCatsFriendsFailureSameCatTest() {
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));

        Assertions.assertThrows(
                SelfException.class,
                () -> catService.makeFriends(testCat.getId(), testCat.getId())
        );
        Mockito.verify(catRepository, Mockito.times(2)).findById(testCat.getId());
    }

    @Test
    public void makeCatsFriendsFailureCatDoesNotExistTest() {
        when(catRepository.findById(testCat.getId())).thenReturn(Optional.empty());

        Assertions.assertThrows(
                CatNotFoundException.class,
                () -> catService.makeFriends(testCat.getId(), testCat.getId() + 10)
        );
        Mockito.verify(catRepository, Mockito.times(1)).findById(testCat.getId());
    }

    @Test
    public void makeCatsFriendsFailureAlreadyFriendsTest() {
        Cat secondTestCat = new Cat(5 , "Kometa", testCatDate, "Sfinks", CatColors.RED, "grey", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));

        catService.makeFriends(testCat.getId(), secondTestCat.getId());

        Assertions.assertThrows(
                AlreadyFriendsException.class,
                () -> catService.makeFriends(testCat.getId(), secondTestCat.getId())
        );
    }

    @Test
    public void unmakeCatsFriendsSuccessTest() {
        Cat secondTestCat = new Cat(5, "Kometa", testCatDate, "Sfinks", CatColors.RED, "yellow", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));
        catService.makeFriends(testCat.getId(), secondTestCat.getId());

        catService.unmakeFriends(testCat.getId(), secondTestCat.getId());

        Mockito.verify(catRepository, Mockito.times(2)).findById(testCat.getId());
        Mockito.verify(catRepository, Mockito.times(2)).findById(secondTestCat.getId());
        Mockito.verify(catRepository, Mockito.times(2)).save(testCat);
        Mockito.verify(catRepository, Mockito.times(2)).save(secondTestCat);
        Assertions.assertFalse(catService.isFriends(testCat.getId(), secondTestCat.getId()));
    }

    @Test
    public void unmakeCatsFriendsFailureCatDoesNotExistTest() {
        Cat secondTestCat = new Cat(5, "Kometa", testCatDate, "Sfinks", CatColors.RED, "blue", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.empty());
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));

        Assertions.assertThrows(
                CatNotFoundException.class,
                () -> catService.unmakeFriends(testCat.getId(), secondTestCat.getId())
        );
        Mockito.verify(catRepository, Mockito.times(1)).findById(testCat.getId());
    }

    @Test
    public void unmakeCatsFriendsFailureCatNotFriendsTest() {
        Cat secondTestCat = new Cat(5, "Kometa", testCatDate, "Sfinks", CatColors.RED, "green", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));

        Assertions.assertThrows(
                NotFriendsException.class,
                () -> catService.unmakeFriends(testCat.getId(), secondTestCat.getId())
        );

        Mockito.verify(catRepository, Mockito.times(1)).findById(testCat.getId());
        Mockito.verify(catRepository, Mockito.times(1)).findById(secondTestCat.getId());
    }

    @Test
    public void getFriendsTest() {
        Cat secondTestCat = new Cat(5, "Kometa", testCatDate, "Sfinks", CatColors.RED, "blue", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));
        catService.makeFriends(testCat.getId(), secondTestCat.getId());

        Assertions.assertEquals(1, catService.getFriends(testCat.getId(), 0, 10).stream().toList().size());
    }

    @Test
    public void isFriendTest() {
        Cat secondTestCat = new Cat(5, "Kometa", testCatDate, "Sfinks", CatColors.RED, "green", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));
        catService.makeFriends(testCat.getId(), secondTestCat.getId());

        Assertions.assertTrue(catService.isFriends(testCat.getId(), secondTestCat.getId()));
    }

    @Test
    public void isFriendsFalseTest() {
        Cat secondTestCat = new Cat(5, "Kometa", testCatDate, "Sfinks", CatColors.RED, "green", owner_id);
        Mockito.when(catRepository.findById(testCat.getId())).thenReturn(Optional.of(testCat));
        Mockito.when(catRepository.findById(secondTestCat.getId())).thenReturn(Optional.of(secondTestCat));

        Assertions.assertFalse(catService.isFriends(testCat.getId(), secondTestCat.getId()));
    }
}