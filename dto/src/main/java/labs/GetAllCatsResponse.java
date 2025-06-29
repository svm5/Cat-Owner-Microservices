package labs;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetAllCatsResponse {
    public List<CatDTO> cats;

    public GetAllCatsResponse() {}
}
