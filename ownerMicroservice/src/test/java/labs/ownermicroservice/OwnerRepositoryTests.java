package labs.ownermicroservice;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import labs.OwnerCriteriaDTO;
import labs.OwnerNotFoundException;
import labs.ownermicroservice.persistence.Owner;
import labs.ownermicroservice.persistence.OwnerRepository;
import labs.ownermicroservice.persistence.OwnerSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class OwnerRepositoryTests {
    private final OwnerRepository ownerRepository;

    @Autowired
    public OwnerRepositoryTests(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:11-alpine")
            .withDatabaseName("cats")
            .withUsername("postgres")
            .withPassword("postgres")
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(9432),
                                    new ExposedPort(5432)))));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeAll
    static void setUp() {
        postgres.start();
    }

    @AfterAll
    static void tearDown() {
        postgres.stop();
    }

    @Test
    public void saveOwnerTest() {
        Owner me = new Owner(1, "Sveta", LocalDate.now());
        Owner testOwner = new Owner(2, "Test owner", LocalDate.now());

        ownerRepository.save(me);
        ownerRepository.save(testOwner);

        List<Owner> owners = ownerRepository.findAll();
        Assertions.assertEquals(2, owners.size());
    }

    @Test
    public void findOwnerByIdWithoutCatsTest() {
        Owner me = new Owner(1, "Sveta", LocalDate.now());
        Owner saved = ownerRepository.save(me);

        Owner fromDB = ownerRepository.findById(saved.getId()).orElseThrow(() -> new OwnerNotFoundException("not found"));

        Assertions.assertEquals(saved.getId(), fromDB.getId());
        Assertions.assertEquals(saved.getName(), fromDB.getName());
        Assertions.assertEquals(saved.getBirthDate(), fromDB.getBirthDate());
    }

    @Test
    public void findAllOwnersByCriteriaTest() {
        Owner me = new Owner(1, "Sveta", LocalDate.now());
        Owner testOwner = new Owner(2, "Test owner", LocalDate.now());
        Owner testOwner2 = new Owner(3, "Test owner", LocalDate.now());
        Owner testOwner3 = new Owner(4, "Test owner", LocalDate.now());

        List<Owner> firstExpected = List.of(testOwner, testOwner2);
        List<Owner> secondExpected = List.of(testOwner3);

        ownerRepository.save(me);
        ownerRepository.save(testOwner);
        ownerRepository.save(testOwner2);
        ownerRepository.save(testOwner3);

        OwnerCriteriaDTO ownerCriteriaDTO = new OwnerCriteriaDTO("Test owner", null);
        Specification<Owner> ownerSpecification = OwnerSpecification.getSpecification(ownerCriteriaDTO);
        PageRequest firstPageRequest = PageRequest.of(0, 2);
        PageRequest secondPageRequest = PageRequest.of(1, 2);

        Page<Owner> firstPage = ownerRepository.findAll(ownerSpecification, firstPageRequest);
        Page<Owner> secondPage = ownerRepository.findAll(ownerSpecification, secondPageRequest);
        List<Owner> first = firstPage.stream().toList();
        List<Owner> second = secondPage.stream().toList();

        Assertions.assertEquals(firstExpected.size(), first.size());
        Assertions.assertEquals(secondExpected.size(), second.size());
    }

    @Test
    public void deleteOwnerByIdTest() {
        Owner owner = new Owner(1, "Petrov Petr", LocalDate.now());
        Owner anotherOwner = new Owner(2, "Ivan", LocalDate.now());

        Owner saved = ownerRepository.save(owner);
        Owner anotherSaved = ownerRepository.save(anotherOwner);

        ownerRepository.deleteById(saved.getId());

        Assertions.assertFalse(ownerRepository.findById(saved.getId()).isPresent());
        Assertions.assertTrue(ownerRepository.findById(anotherSaved.getId()).isPresent());
    }

    @Test
    public void deleteAllOwnersIdTest() {
        Owner owner = new Owner(1, "Petrov Petr", LocalDate.now());
        Owner anotherOwner = new Owner(2, "Ivan", LocalDate.now());

        ownerRepository.save(owner);
        ownerRepository.save(anotherOwner);

        ownerRepository.deleteAll();

        Assertions.assertEquals(0, ownerRepository.findAll().size());
    }
}