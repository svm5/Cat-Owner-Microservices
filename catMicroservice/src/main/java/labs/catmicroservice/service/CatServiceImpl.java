package labs.catmicroservice.service;


import labs.*;
import labs.catmicroservice.persistence.Cat;

import labs.catmicroservice.persistence.CatRepository;
import labs.catmicroservice.persistence.CatSpecification;
import labs.CatDTO;
import labs.CreateCatDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatServiceImpl implements CatService {
    @Autowired
    private CatRepository catRepository;

    @Autowired
    private CatDTOMapping catDTOMapping;

    public CatDTO createCat(CreateCatDTO createCatDTO) {
        Cat cat = catDTOMapping.fromCreateDTO(createCatDTO);
        return CatDTOMapping.toDTO(catRepository.save(cat));
    }

    public CatDTO getCat(long id) {
        return CatDTOMapping.toDTO(findCat(id));
    }

    public Page<CatDTO> getCats(CatCriteriaDTO catCriteriaDTO, int page, int size) {
        Specification<Cat> catSpecification = CatSpecification.getSpecification(catCriteriaDTO);
        PageRequest pageRequest = PageRequest.of(page, size);
        return catRepository.findAll(catSpecification, pageRequest).map(CatDTOMapping::toDTO);
    }

    @Transactional
    public void changeOwner(long catId, long newOwnerId) {
        Cat cat = findCat(catId);
        cat.setOwner_id(newOwnerId);
        catRepository.save(cat);
    }

    public void deleteCat(long id) {
        catRepository.deleteById(id);
    }

    public void deleteCats() {
        catRepository.deleteAll();
    }

    @Transactional
    public void makeFriends(long firstCatId, long secondCatId) {
        Cat firstCat = findCat(firstCatId);
        Cat secondCat = findCat(secondCatId);

        if (firstCatId == secondCatId) {
            throw new SelfException("You are not allowed to be friends with yourself");
        }

        if (firstCat.getFriends().contains(secondCat)) {
            throw new AlreadyFriendsException("Cat with id" + firstCatId + "and cat with id " + secondCatId + " are already friends");
        }

        firstCat.addFriend(secondCat);
        secondCat.addFriend(firstCat);
        catRepository.save(firstCat);
        catRepository.save(secondCat);
    }

    @Transactional
    public void unmakeFriends(long firstCatId, long secondCatId) {
        Cat firstCat = findCat(firstCatId);
        Cat secondCat = findCat(secondCatId);

        if (!firstCat.getFriends().contains(secondCat)) {
            throw new NotFriendsException("Cat with id" + firstCatId + "and cat with id " + secondCatId + " are not friends");
        }

        firstCat.removeFriend(secondCat);
        secondCat.removeFriend(firstCat);
        catRepository.save(firstCat);
        catRepository.save(secondCat);
    }

    @Transactional
    public Page<CatDTO> getFriends(long id, int page, int size) {
        Cat cat = findCat(id);
        PageRequest pageRequest = PageRequest.of(page, size);
        List<CatDTO> friends = cat.getFriends().stream().map(CatDTOMapping::toDTO).toList();

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + size), friends.size());
        List<CatDTO> pageContent = friends.subList(start, end);

        return new PageImpl<>(pageContent, pageRequest, size);
    }

    @Transactional
    public boolean isFriends(long firstCatId, long secondCatId) {
        Cat firstCat = findCat(firstCatId);
        Cat secondCat = findCat(secondCatId);

        return firstCat.getFriends().contains(secondCat);
    }

    private Cat findCat(long id) throws CatNotFoundException {
        return catRepository.findById(id)
                .orElseThrow(() -> new CatNotFoundException("Cat with id " + id + " not found"));
    }
}
