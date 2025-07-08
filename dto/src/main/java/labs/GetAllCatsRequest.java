package labs;

import lombok.Builder;

@Builder
public record GetAllCatsRequest(CatCriteriaDTO catCriteriaDTO, Integer page, Integer size) {
}
