package labs;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetOwnerDTO {
    public OwnerDTO ownerDTO;

    public String error;

    public GetOwnerDTO() {}
}
