package labs.ownermicroservice.persistence;

import jakarta.persistence.criteria.Predicate;
import labs.OwnerCriteriaDTO;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OwnerSpecification {
    public static Specification<Owner> getSpecification(OwnerCriteriaDTO ownerCriteriaDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ownerCriteriaDto.name() != null) {
                predicates.add(criteriaBuilder.equal(root.get("name"), ownerCriteriaDto.name()));
            }
            if (ownerCriteriaDto.birthday() != null) {
                predicates.add(criteriaBuilder.equal(root.get("birthday"), ownerCriteriaDto.birthday()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
