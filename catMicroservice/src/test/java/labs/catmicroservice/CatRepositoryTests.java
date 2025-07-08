package labs.catmicroservice;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import labs.CatColors;
import labs.CatCriteriaDTO;
import labs.catmicroservice.persistence.Cat;
import labs.catmicroservice.persistence.CatRepository;
import labs.catmicroservice.persistence.CatSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
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
public class CatRepositoryTests {
    @Autowired
    CatRepository catRepository;

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
    public void saveCatTest() {
        Cat barsik = new Cat("Barsik", LocalDate.now(), "Sfinks", CatColors.RED, "green", 1);
        Cat kuzia = new Cat("Kuzia", LocalDate.now(), "Maine Coon", CatColors.MULTICOLOURED, "blue", 1);

        catRepository.save(barsik);
        catRepository.save(kuzia);

        Assertions.assertEquals(2, catRepository.findAll().size());
        Assertions.assertEquals(barsik, catRepository.findById(barsik.getId()).get());
        Assertions.assertEquals(kuzia, catRepository.findById(kuzia.getId()).get());
    }

    @Test
    public void findAllCatsByOneCriteriaTest() {
        Cat barsik = new Cat("Barsik", LocalDate.now(), "Sfinks", CatColors.RED, "green", 1);
        Cat kuzia = new Cat("Kuzia", LocalDate.now(), "Maine Coon", CatColors.MULTICOLOURED, "blue", 1);
        Cat cat = new Cat("Cat", LocalDate.now(), "Persian", CatColors.RED, "green", 1);

        List<Cat> expected = List.of(barsik, cat);

        catRepository.save(barsik);
        catRepository.save(kuzia);
        catRepository.save(cat);

        CatCriteriaDTO catCriteriaDTO = new CatCriteriaDTO(null, null, null, CatColors.RED, null);
        Specification<Cat> catSpecification = CatSpecification.getSpecification(catCriteriaDTO);
        PageRequest pageRequest = PageRequest.of(0, 2);

        Page<Cat> cats = catRepository.findAll(catSpecification, pageRequest);
        List<Cat> catList = cats.stream().toList();

        Assertions.assertEquals(2, cats.getTotalElements());
        Assertions.assertEquals(expected, catList);
    }

    @Test
    public void findAllCatsByMultipleCriteriaTest() {
        Cat barsik = new Cat("Barsik", LocalDate.now(), "Sfinks", CatColors.RED, "blue", 1);
        Cat kuzia = new Cat("Kuzia", LocalDate.now(), "Maine Coon", CatColors.MULTICOLOURED, "blue", 1);
        Cat cat = new Cat("Cat", LocalDate.now(), "Persian", CatColors.RED, "green", 1);
        Cat cat2 = new Cat("Cat2", LocalDate.now(), "Persian", CatColors.RED, "blue", 1);

        List<Cat> expected = List.of(barsik, cat2);

        catRepository.save(barsik);
        catRepository.save(kuzia);
        catRepository.save(cat);
        catRepository.save(cat2);

        CatCriteriaDTO catCriteriaDTO = new CatCriteriaDTO(null, null, null, CatColors.RED, "blue");
        Specification<Cat> catSpecification = CatSpecification.getSpecification(catCriteriaDTO);
        PageRequest pageRequest = PageRequest.of(0, 2);

        Page<Cat> cats = catRepository.findAll(catSpecification, pageRequest);
        List<Cat> catList = cats.stream().toList();

        Assertions.assertEquals(2, cats.getTotalElements());
        Assertions.assertEquals(expected, catList);
    }

    @Test
    public void deleteCatByIdTest() {
        Cat barsik = new Cat("Barsik", LocalDate.now(), "Sfinks", CatColors.RED, "blue", 1);
        Cat kuzia = new Cat("Kuzia", LocalDate.now(), "Maine Coon", CatColors.MULTICOLOURED, "blue", 1);

        Cat saved1 = catRepository.save(barsik);
        Cat saved2 = catRepository.save(kuzia);

        catRepository.deleteById(saved1.getId());

        Assertions.assertFalse(catRepository.findById(saved1.getId()).isPresent());
        Assertions.assertTrue(catRepository.findById(saved2.getId()).isPresent());
    }

    @Test
    public void deleteAllCatsTest() {
        Cat barsik = new Cat("Barsik", LocalDate.now(), "Sfinks", CatColors.RED, "blue", 1);
        Cat kuzia = new Cat("Kuzia", LocalDate.now(), "Maine Coon", CatColors.MULTICOLOURED, "blue", 1);

        Cat saved1 = catRepository.save(barsik);
        Cat saved2 = catRepository.save(kuzia);

        catRepository.deleteAll();

        Assertions.assertTrue(catRepository.findAll().isEmpty());
    }
}