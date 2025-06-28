package labs;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record OwnerCriteriaDTO(String name, LocalDate birthday) {
}
