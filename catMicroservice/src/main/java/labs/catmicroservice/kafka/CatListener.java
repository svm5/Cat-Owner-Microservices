package labs.catmicroservice.kafka;

import labs.*;
import labs.catmicroservice.persistence.CatRepository;
import labs.catmicroservice.service.CatService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatListener {
    @Autowired
    private CatService catService;

    @KafkaListener(topics = "create_cat_request", groupId = "group1",
            containerFactory = "createCatRequestListenerContainerFactory")
    @SendTo
    public CatDTO processCreateCat(CreateCatDTO data) {
        System.out.println("Want to create cat");

        return catService.createCat(data);
    }

    @KafkaListener(topics = "get_cat_request", groupId = "group1",
            containerFactory = "getCatRequestListenerContainerFactory")
    @SendTo
    public GetCatDTO processGetCat(Long id) {
        System.out.println("Want to get cat");

        try {
            return new GetCatDTO(catService.getCat(id), "");
        }
        catch (CatNotFoundException e) {
            return new GetCatDTO(null, e.getMessage());
        }
    }

    @KafkaListener(topics = "get_all_cats_request", groupId = "group1",
            containerFactory = "getAllCatsRequestListenerContainerFactory")
    @SendTo
    public GetAllCatsResponse processGetAllCats(GetAllCatsRequest getAllCatsRequest) {
        System.out.println("Want to get all cats");

        List<CatDTO> page = catService.getCats(
                getAllCatsRequest.catCriteriaDTO(),
                getAllCatsRequest.page(),
                getAllCatsRequest.size())
                .stream()
                .toList();

        System.out.println("page" + page);

        return new GetAllCatsResponse(page);
    }

    @KafkaListener(topics = "make_friends_request", groupId = "group1",
            containerFactory = "friendsRequestListenerContainerFactory")
    @SendTo
    public CatsFriendsResponse processMakeFriends(CatsFriendsRequest catsFriendsRequest) {
        System.out.println("Want to make friends");
        String errorMessage = "";
        try {
            catService.makeFriends(catsFriendsRequest.firstId(), catsFriendsRequest.secondId());
        }
        catch (Exception e) {
            errorMessage = e.getMessage();
        }

        return new CatsFriendsResponse(errorMessage);
    }

    @KafkaListener(topics = "unmake_friends_request", groupId = "group1",
            containerFactory = "friendsRequestListenerContainerFactory")
    @SendTo
    public CatsFriendsResponse processUnmakeFriends(CatsFriendsRequest catsFriendsRequest) {
        String errorMessage = "";
        try {
            catService.unmakeFriends(catsFriendsRequest.firstId(), catsFriendsRequest.secondId());
        }
        catch (Exception e) {
            errorMessage = e.getMessage();
        }

        return new CatsFriendsResponse(errorMessage);
    }

    @KafkaListener(topics = "change_owner_request", groupId = "group1",
            containerFactory = "changeOwnerRequestListenerContainerFactory")
    @SendTo
    public ChangeOwnerResponse processChangeOwner(ChangeOwnerRequest changeOwnerRequest) {
        String errorMessage = "";
        try {
            catService.changeOwner(changeOwnerRequest.catId(), changeOwnerRequest.newOwnerId());
        }
        catch (Exception e) {
            errorMessage = e.getMessage();
        }

        return new ChangeOwnerResponse(errorMessage);
    }

    @KafkaListener(topics = "delete_cat_by_id", groupId = "group1", containerFactory = "requestListenerContainerFactory")
    public void processDeleteOwnerById(@Payload Long catId) {
        System.out.println("Want to delete cat by id: " + catId);
        catService.deleteCat(catId);
    }

    @KafkaListener(topics = "delete_all_cats", groupId = "group1")
    public void processDeleteAllOwners(@Payload String s) {
        System.out.println("Want to delete all: ");
        catService.deleteCats();
    }
}
