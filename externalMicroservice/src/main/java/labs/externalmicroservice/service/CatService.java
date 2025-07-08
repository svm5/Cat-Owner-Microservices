package labs.externalmicroservice.service;

import labs.*;

public interface CatService {
    GetAllCatsResponse getAllCats(GetAllCatsRequest getAllCatsRequest);
    CatDTO createCat(CreateCatDTO CreateCatDTO);
    GetCatDTO getCatById(Long id);
    void deleteCatById(Long id);
    void deleteAllCats();
    CatsFriendsResponse makeFriends(CatsFriendsRequest catsFriendsRequest, boolean make);
    ChangeOwnerResponse changeOwner(ChangeOwnerRequest changeOwnerRequest);
}
