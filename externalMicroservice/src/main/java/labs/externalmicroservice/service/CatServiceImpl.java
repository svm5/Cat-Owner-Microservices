package labs.externalmicroservice.service;

import labs.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CatServiceImpl implements CatService {
    private final ReplyingKafkaTemplate<String, GetAllCatsRequest, GetAllCatsResponse> getAllCatsRequestReplyKafkaTemplate;
    private final ReplyingKafkaTemplate<String, CreateCatDTO, CatDTO> createCatReplyingKafkaTemplate;
    private final ReplyingKafkaTemplate<String, Long, GetCatDTO> getCatReplyingKafkaTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, Long> stringLongKafkaTemplate;
    private final ReplyingKafkaTemplate<String, CatsFriendsRequest, CatsFriendsResponse> friendsRequestReplyKafkaTemplate;
    private final ReplyingKafkaTemplate<String, ChangeOwnerRequest, ChangeOwnerResponse> changeOwnerReplyKafkaTemplate;

    @Autowired
    public CatServiceImpl(ReplyingKafkaTemplate<String, GetAllCatsRequest, GetAllCatsResponse> getAllCatsRequestReplyKafkaTemplate,
                          ReplyingKafkaTemplate<String, CreateCatDTO, CatDTO> createCatReplyingKafkaTemplate,
                          ReplyingKafkaTemplate<String, Long, GetCatDTO> getCatReplyingKafkaTemplate,
                          KafkaTemplate<String, String> kafkaTemplate,
                          KafkaTemplate<String, Long> stringLongKafkaTemplate,
                          ReplyingKafkaTemplate<String, CatsFriendsRequest, CatsFriendsResponse> friendsRequestReplyKafkaTemplate,
                          ReplyingKafkaTemplate<String, ChangeOwnerRequest, ChangeOwnerResponse> changeOwnerReplyKafkaTemplate) {
        this.getAllCatsRequestReplyKafkaTemplate = getAllCatsRequestReplyKafkaTemplate;
        this.createCatReplyingKafkaTemplate = createCatReplyingKafkaTemplate;
        this.getCatReplyingKafkaTemplate = getCatReplyingKafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.stringLongKafkaTemplate = stringLongKafkaTemplate;
        this.friendsRequestReplyKafkaTemplate = friendsRequestReplyKafkaTemplate;
        this.changeOwnerReplyKafkaTemplate = changeOwnerReplyKafkaTemplate;
    }

    public GetAllCatsResponse getAllCats(GetAllCatsRequest getAllCatsRequest) {
        ProducerRecord<String, GetAllCatsRequest> record = new ProducerRecord<String, GetAllCatsRequest>("get_all_cats_request", getAllCatsRequest);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_all_cats_response".getBytes()))
                .add(new RecordHeader(KafkaHeaders.CORRELATION_ID, UUID.randomUUID().toString().getBytes()));
        RequestReplyFuture<String, GetAllCatsRequest, GetAllCatsResponse> sendAndReceive = getAllCatsRequestReplyKafkaTemplate.sendAndReceive(record);

        try {
            return sendAndReceive.get(70, TimeUnit.SECONDS).value();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CatDTO createCat(CreateCatDTO createCatDTO) {
        ProducerRecord<String, CreateCatDTO> record = new ProducerRecord<String, CreateCatDTO>("create_cat_request", createCatDTO);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "create_cat_response".getBytes()));
        RequestReplyFuture<String, CreateCatDTO, CatDTO> sendAndReceive = createCatReplyingKafkaTemplate.sendAndReceive(record);
        try {
            return sendAndReceive.get(70, TimeUnit.SECONDS).value();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetCatDTO getCatById(Long id) {
        ProducerRecord<String, Long> record = new ProducerRecord<String, Long>("get_cat_request", id);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_cat_response".getBytes()));
        RequestReplyFuture<String, Long, GetCatDTO> sendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(record);
        try {
            return sendAndReceive.get(70, TimeUnit.SECONDS).value();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCatById(Long id) {
        stringLongKafkaTemplate.send("delete_cat_by_id", id);
    }

    public void deleteAllCats() {
        kafkaTemplate.send("delete_all_owners", "delete");
    }

    public CatsFriendsResponse makeFriends(CatsFriendsRequest catsFriendsRequest, boolean make) {
        String requestTopicName = "make_friends_request";
        String responseTopicName = "make_friends_response";
        if (!make) {
            requestTopicName = "unmake_friends_request";
            responseTopicName = "unmake_friends_response";
        }
        ProducerRecord<String, CatsFriendsRequest> record = new ProducerRecord<String, CatsFriendsRequest>(requestTopicName, catsFriendsRequest);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, responseTopicName.getBytes()));
        RequestReplyFuture<String, CatsFriendsRequest, CatsFriendsResponse> sendAndReceive = friendsRequestReplyKafkaTemplate.sendAndReceive(record);
        try {
            return sendAndReceive.get(70, TimeUnit.SECONDS).value();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public ChangeOwnerResponse changeOwner(ChangeOwnerRequest changeOwnerRequest) {
        ProducerRecord<String, ChangeOwnerRequest> changeOwnerRecord = new ProducerRecord<>(
                "change_owner_request",
                new ChangeOwnerRequest(changeOwnerRequest.catId(), changeOwnerRequest.newOwnerId()));
        changeOwnerRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "change_owner_response".getBytes()));
        RequestReplyFuture<String, ChangeOwnerRequest, ChangeOwnerResponse> changeOwnerSendAndReceive = changeOwnerReplyKafkaTemplate.sendAndReceive(changeOwnerRecord);
        try {
            return changeOwnerSendAndReceive.get(70, TimeUnit.SECONDS).value();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
