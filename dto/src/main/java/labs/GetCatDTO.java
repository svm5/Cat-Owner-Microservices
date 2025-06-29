package labs;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetCatDTO {
    public CatDTO catDTO;

    public String error;

    public GetCatDTO() {}
}
