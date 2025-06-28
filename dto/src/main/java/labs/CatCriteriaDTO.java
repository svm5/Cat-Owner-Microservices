package labs;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CatCriteriaDTO(String name, LocalDate birthday, String breed, CatColors color, String eyesColor) {
}
