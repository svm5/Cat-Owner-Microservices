package labs.catmicroservice.service;


import labs.CatCriteriaDTO;
import labs.CatDTO;
import labs.CreateCatDTO;
import org.springframework.data.domain.Page;

public interface CatService {
    CatDTO createCat(CreateCatDTO createCatDTO);
    CatDTO getCat(long id);
    Page<CatDTO> getCats(CatCriteriaDTO catCriteriaDTO, int page, int size);
    void changeOwner(long catId, long newOwnerId);
    void deleteCat(long id);
    void deleteCats();
    void makeFriends(long firstCatId, long secondCatId);
    void unmakeFriends(long firstCatId, long secondCatId);
    Page<CatDTO> getFriends(long id, int page, int size);
    boolean isFriends(long firstCatId, long secondCatId);
}
