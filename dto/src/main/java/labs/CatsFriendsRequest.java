package labs;

import lombok.Builder;

@Builder
public record CatsFriendsRequest(Long firstId, Long secondId) {
}
