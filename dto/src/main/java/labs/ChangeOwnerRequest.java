package labs;

import lombok.Builder;

@Builder
public record ChangeOwnerRequest(Long catId, Long newOwnerId) {
}
