package labs.externalmicroservice.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import labs.*;
import labs.externalmicroservice.service.UserService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/cats")
@Tag(name = "Cats", description = "Управление котами")
@SecurityRequirement(name = "basicAuth")
public class CatController {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, Long> stringLongKafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate < String, Long, GetOwnerDTO > getOwnerKafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, CreateCatDTO, CatDTO> createCatReplyingKafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, Long, CatDTO> getCatReplyingKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, CatsFriendsRequest> catsFriendsRequestKafkaTemplate;

    @Autowired
    private UserService userService;

    @PostMapping(consumes = {"application/json"})
    @Operation(summary = "Создание кота")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CompletableFuture<ResponseEntity<CatDTO>> createCat(@RequestBody CreateCatDTO createCatDTO) {
        if (!userService.checkUserAuthenticated(createCatDTO.owner_id) && !userService.checkAdmin()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        }

        return CompletableFuture.supplyAsync(() -> {
            // check if owner exists
            ProducerRecord<String, Long> recordOwner = new ProducerRecord<String, Long>("get_owner_by_id_request", createCatDTO.owner_id);
            recordOwner.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_owner_by_id_response".getBytes()));
            RequestReplyFuture<String, Long, GetOwnerDTO> sendAndReceiveOwner = getOwnerKafkaTemplate.sendAndReceive(recordOwner);
            try {
                GetOwnerDTO result = sendAndReceiveOwner.get(70, TimeUnit.SECONDS).value();
                if (result.ownerDTO == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }

            // create cat
            ProducerRecord<String, CreateCatDTO> record = new ProducerRecord<String, CreateCatDTO>("create_cat_request", createCatDTO);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "create_cat_response".getBytes()));
            RequestReplyFuture<String, CreateCatDTO, CatDTO> sendAndReceive = createCatReplyingKafkaTemplate.sendAndReceive(record);
            try {
                CatDTO catDTO = sendAndReceive.get(70, TimeUnit.SECONDS).value();
                return new ResponseEntity<>(catDTO, HttpStatus.CREATED);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение кота по id")
    public CompletableFuture<ResponseEntity<CatDTO>> getCat(@PathVariable("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            ProducerRecord<String, Long> record = new ProducerRecord<String, Long>("get_cat_request", id);
            // устанавливаем топик для ответа в заголовке
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_cat_response".getBytes()));
            // отправляем запрос в топик Kafka и асинхронно получаем ответ в указанный топик для ответа
            RequestReplyFuture<String, Long, CatDTO> sendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(record);
            try {
                CatDTO catDTO = sendAndReceive.get(70, TimeUnit.SECONDS).value();
                if (!(userService.checkUserAuthenticated(catDTO.ownerId, authentication) || userService.checkAdmin())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                return new ResponseEntity<>(catDTO, HttpStatus.OK);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }

    @DeleteMapping({"/{id}"})
    @Operation(summary = "Удаление конкретного кота")
//    @PreAuthorize("hasRole('ROLE_ADMIN') || @catOwnerChecker.check(#id)")
    public CompletableFuture<ResponseEntity<Object>> deleteCat(@PathVariable long id) {
        if (!userService.checkAdmin()) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                SendResult<String, Long> result = stringLongKafkaTemplate.send("delete_cat_by_id", id).get();
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
    @Operation(summary = "Удаление всех котов")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAllCats() {
        if (!userService.checkAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        kafkaTemplate.send("delete_all_owners", "delete");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/friends/make")
    @Operation(summary = "Подружить двух котов")
//    @PreAuthorize("hasRole('ROLE_ADMIN') || @catFriendsRequestChecker.check(#firstCatId, #secondCatId)")
    public void makeFriends(
            @RequestParam(value = "firstCatId") Long firstCatId,
            @RequestParam(value = "secondCatId") Long secondCatId) {
//        catService.makeFriends(firstCatId, secondCatId);
        catsFriendsRequestKafkaTemplate.send("make_friends", new CatsFriendsRequest(firstCatId, secondCatId));
    }

    @PostMapping("/friends/unmake")
    @Operation(summary = "Прекратить дружбу двух котов")
//    @PreAuthorize("hasRole('ROLE_ADMIN') || @catFriendsRequestChecker.check(#firstCatId, #secondCatId)")
    public void unmakeFriends(
            @RequestParam(value = "firstCatId") long firstCatId,
            @RequestParam(value = "secondCatId") long secondCatId) {
        catsFriendsRequestKafkaTemplate.send("unmake_friends", new CatsFriendsRequest(firstCatId, secondCatId));
    }
}
