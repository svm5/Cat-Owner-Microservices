package labs.ownermicroservice.kafka;

import labs.*;
import labs.ownermicroservice.service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OwnerListener {
    @Autowired
    private OwnerService ownerService;

//    private PagedResourcesAssembler<OwnerDTO> pagedAssembler;


    @KafkaListener(topics = "create_owner", groupId = "group1",
            containerFactory = "createOwnerDTOKafkaListenerContainerFactory")
    void listener(CreateOwnerDTO data) {
        System.out.println("Want to create owner: " + data.name + " " + data.birthday);
        ownerService.createOwner(data);
    }

    @KafkaListener(topics = "get_owner_by_id_request", groupId = "group1", containerFactory = "requestListenerContainerFactory")
    @SendTo("get_owner_by_id_response")
    public GetOwnerDTO receive(@Payload Long ownerId,
                               @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTo,
                               @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        System.out.println("Want to get owner by id: " + ownerId);
        // ...
        try {
            return new GetOwnerDTO(ownerService.getOwner(ownerId), "");
        }
        catch (OwnerNotFoundException e) {
            System.out.println("Owner not found");
            return new GetOwnerDTO(null, "Owner with id " + ownerId + " not found");
        }
//        return new OwnerDTO(1, "abc", LocalDate.of(2000, 10, 10), new ArrayList<>());
    }

    @KafkaListener(topics = "delete_owner_by_id", groupId = "group1", containerFactory = "requestListenerContainerFactory")
    public void processDeleteOwnerById(@Payload Long ownerId) {
        System.out.println("Want to delete owner by id: " + ownerId);
        ownerService.deleteOwner(ownerId);
    }

    @KafkaListener(topics = "delete_all_owners", groupId = "group1")
    public void processDeleteAllOwners(@Payload String s) {
        System.out.println("Want to delete all: ");
        ownerService.deleteAllOwners();
    }

    @KafkaListener(topics = "get_all_owners_request", groupId = "group1", containerFactory = "getAllOwnersRequestListenerContainerFactory")
    @SendTo
    public Message<List<OwnerDTO>> processGetAllOwners(@Payload GetAllOwnersRequest getAllOwnersRequest,
                                                       @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTo,
                                                       @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        System.out.println("Want to get all owners");
        List<OwnerDTO> page = ownerService.getAllOwners(
                getAllOwnersRequest.ownerCriteriaDTO(),
                getAllOwnersRequest.page(),
                getAllOwnersRequest.size()).stream().toList();
        System.out.println("page" + page);

//        return page;

        return MessageBuilder
                .withPayload(page)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId) // Возвращаем тот же ID
                .build();
    }
}
