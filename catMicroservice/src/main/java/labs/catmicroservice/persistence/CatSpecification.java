package labs.catmicroservice.persistence;

import jakarta.persistence.criteria.Predicate;
import labs.CatCriteriaDTO;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CatSpecification {
    public static Specification<Cat> getSpecification(CatCriteriaDTO catCriteriaDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (catCriteriaDTO.name() != null) {
                predicates.add(criteriaBuilder.equal(root.get("name"), catCriteriaDTO.name()));
            }
            if (catCriteriaDTO.birthday() != null) {
                predicates.add(criteriaBuilder.equal(root.get("birthDate"), catCriteriaDTO.birthday()));
            }
            if (catCriteriaDTO.breed() != null) {
                predicates.add(criteriaBuilder.equal(root.get("breedName"), catCriteriaDTO.breed()));
            }
            if (catCriteriaDTO.color() != null) {
                predicates.add(criteriaBuilder.equal(root.get("color"), catCriteriaDTO.color()));
            }
            if (catCriteriaDTO.eyesColor() != null) {
                predicates.add(criteriaBuilder.equal(root.get("eyesColor"), catCriteriaDTO.eyesColor()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
