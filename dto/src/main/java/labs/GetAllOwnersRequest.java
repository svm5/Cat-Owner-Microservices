package labs;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GetAllOwnersRequest(OwnerCriteriaDTO ownerCriteriaDTO, Integer page, Integer size) {
}
