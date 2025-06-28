package labs.externalmicroservice.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import labs.*;
import labs.externalmicroservice.service.UserService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/owners")
@Tag(name = "Owners", description = "Управление владельцами")
@SecurityRequirement(name = "basicAuth")
public class OwnerController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, CreateOwnerDTO> createOwnerKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, Long> stringLongKafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate < String, Long, GetOwnerDTO > requestReplyKafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, GetAllOwnersRequest, List<OwnerDTO>> getAllOwnersKafkaTemplate;

    @Autowired
    private UserService userService;
//    @PostMapping()
//    public void test(CreateOwnerDTO createOwnerDTO) {
//        createOwnerKafkaTemplate.send("create_owner", createOwnerDTO);
//    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение владельца по id")
    public CompletableFuture<ResponseEntity<OwnerDTO>> getOwner(@PathVariable Long id) {
        if (!userService.checkUserAuthenticated(id) && !userService.checkAdmin()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        }

        return CompletableFuture.supplyAsync(() -> {
            ProducerRecord<String, Long> record = new ProducerRecord<String, Long>("get_owner_by_id_request", id);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_owner_by_id_response".getBytes()));
            RequestReplyFuture<String, Long, GetOwnerDTO> sendAndReceive = requestReplyKafkaTemplate.sendAndReceive(record);
            try {
                GetOwnerDTO result = sendAndReceive.get(70, TimeUnit.SECONDS).value();
                if (result.ownerDTO == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                return new ResponseEntity<>(result.ownerDTO, HttpStatus.OK);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }

    @GetMapping
    @Operation(summary = "Получение всех владельцев")
    public CompletableFuture<ResponseEntity<List<OwnerDTO>>> getAllOwners(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "birthday", required = false) String birthday,
            @RequestParam(name = "page", defaultValue = "0") String page,
            @RequestParam(name = "size", defaultValue = "3") String size) {
        OwnerCriteriaDTO.OwnerCriteriaDTOBuilder ownerCriteriaDTOBuilder = OwnerCriteriaDTO.builder().name(name);
        if (birthday != null) {
            ownerCriteriaDTOBuilder = ownerCriteriaDTOBuilder.birthday(LocalDate.parse(birthday));
        }

        GetAllOwnersRequest getAllOwnersRequest = GetAllOwnersRequest
                .builder()
                .ownerCriteriaDTO(ownerCriteriaDTOBuilder.build())
                .page(Integer.parseInt(page))
                .size(Integer.parseInt(size))
                .build();

        return CompletableFuture.supplyAsync(() -> {
            ProducerRecord<String, GetAllOwnersRequest> record = new ProducerRecord<String, GetAllOwnersRequest>("get_all_owners_request", getAllOwnersRequest);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_all_owners_response".getBytes()))
                            .add(new RecordHeader(KafkaHeaders.CORRELATION_ID, UUID.randomUUID().toString().getBytes()));
            RequestReplyFuture<String, GetAllOwnersRequest, List<OwnerDTO>> sendAndReceive = getAllOwnersKafkaTemplate.sendAndReceive(record);
            try {
                return new ResponseEntity<>(sendAndReceive.get(30, TimeUnit.SECONDS).value(), HttpStatus.OK);
            } catch (Exception e) {
                System.out.println("!!!Error " + e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление конкретного владельца")
    public CompletableFuture<ResponseEntity<Object>> deleteOwner(@PathVariable Long id) {
        if (!userService.checkUserAuthenticated(id) && !userService.checkAdmin()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        }
        return CompletableFuture.supplyAsync(() -> {
                try {
                    // Отправляем и ждем подтверждения
                    SendResult<String, Long> result = stringLongKafkaTemplate.send("delete_owner_by_id", id).get();
                    return ResponseEntity.ok().build();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Error", e);
                }
            })
            .exceptionally(ex -> {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            });
    }

    @DeleteMapping
    @Operation(summary = "Удаление всех владельцев")
    public ResponseEntity<Void> deleteAllOwners() {
        if (!userService.checkAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        kafkaTemplate.send("delete_all_owners", "delete");
        return ResponseEntity.noContent().build();
    }
}

