package labs;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetAllOwnersResponse {
    public List<OwnerDTO> owners;

    public GetAllOwnersResponse() {}
}
