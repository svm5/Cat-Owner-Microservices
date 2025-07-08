package labs.externalmicroservice.service;

import labs.GetAllOwnersRequest;
import labs.GetAllOwnersResponse;
import labs.GetOwnerDTO;
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
public class OwnerServiceImpl implements OwnerService {
    private final ReplyingKafkaTemplate<String, Long, GetOwnerDTO> requestReplyKafkaTemplate;
    private final ReplyingKafkaTemplate<String, GetAllOwnersRequest, GetAllOwnersResponse> getAllOwnersKafkaTemplate;
    private final KafkaTemplate<String, Long> stringLongKafkaTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public OwnerServiceImpl(ReplyingKafkaTemplate<String, Long, GetOwnerDTO> requestReplyKafkaTemplate,
                            ReplyingKafkaTemplate<String, GetAllOwnersRequest, GetAllOwnersResponse> getAllOwnersKafkaTemplate,
                            KafkaTemplate<String, Long> stringLongKafkaTemplate,
                            KafkaTemplate<String, String> kafkaTemplate) {
        this.requestReplyKafkaTemplate = requestReplyKafkaTemplate;
        this.getAllOwnersKafkaTemplate = getAllOwnersKafkaTemplate;
        this.stringLongKafkaTemplate = stringLongKafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public GetOwnerDTO getOwner(Long id) {
        ProducerRecord<String, Long> record = new ProducerRecord<String, Long>("get_owner_by_id_request", id);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_owner_by_id_response".getBytes()));
        RequestReplyFuture<String, Long, GetOwnerDTO> sendAndReceive = requestReplyKafkaTemplate.sendAndReceive(record);

        try {
            return sendAndReceive.get(70, TimeUnit.SECONDS).value();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public GetAllOwnersResponse getAllOwners(GetAllOwnersRequest request) {
        ProducerRecord<String, GetAllOwnersRequest> record = new ProducerRecord<String, GetAllOwnersRequest>("get_all_owners_request", request);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_all_owners_response".getBytes()))
                .add(new RecordHeader(KafkaHeaders.CORRELATION_ID, UUID.randomUUID().toString().getBytes()));
        RequestReplyFuture<String, GetAllOwnersRequest, GetAllOwnersResponse> sendAndReceive = getAllOwnersKafkaTemplate.sendAndReceive(record);
        try {
            return sendAndReceive.get(70, TimeUnit.SECONDS).value();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteOwnerById(Long id) {
        stringLongKafkaTemplate.send("delete_owner_by_id", id);
    }

    public void deleteAllOwners() {
        kafkaTemplate.send("delete_all_owners", "delete");
    }
}
