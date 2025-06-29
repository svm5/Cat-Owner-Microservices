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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
    private ReplyingKafkaTemplate<String, Long, GetCatDTO> getCatReplyingKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, CatsFriendsRequest> catsFriendsRequestKafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, CatsFriendsRequest, CatsFriendsResponse> friendsRequestReplyKafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate<String, GetAllCatsRequest, GetAllCatsResponse> getAllCatsRequestReplyKafkaTemplate;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Получение всех котов")
    public CompletableFuture<ResponseEntity<List<CatDTO>>> getAllCats(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "birthday", required = false) String birthday,
            @RequestParam(name = "breed", required = false) String breed,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "eyesColor", required = false) String eyesColor,
            @RequestParam(name = "page", defaultValue = "0") String page,
            @RequestParam(name = "size", defaultValue = "3") String size) {

        CatCriteriaDTO.CatCriteriaDTOBuilder catCriteriaDTOBuilder = CatCriteriaDTO.builder()
                .name(name)
                .eyesColor(eyesColor)
                .breed(breed);
        if (birthday != null) {
            catCriteriaDTOBuilder.birthday(LocalDate.parse(birthday));
        }
        if (color != null) {
            catCriteriaDTOBuilder.color(CatColors.valueOf(color));
        }

        GetAllCatsRequest getAllCatsRequest = GetAllCatsRequest
                .builder()
                .catCriteriaDTO(catCriteriaDTOBuilder.build())
                .page(Integer.parseInt(page))
                .size(Integer.parseInt(size))
                .build();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            ProducerRecord<String, GetAllCatsRequest> record = new ProducerRecord<String, GetAllCatsRequest>("get_all_cats_request", getAllCatsRequest);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_all_cats_response".getBytes()))
                    .add(new RecordHeader(KafkaHeaders.CORRELATION_ID, UUID.randomUUID().toString().getBytes()));
            RequestReplyFuture<String, GetAllCatsRequest, GetAllCatsResponse> sendAndReceive = getAllCatsRequestReplyKafkaTemplate.sendAndReceive(record);
            try {
                List<CatDTO> result = sendAndReceive.get(70, TimeUnit.SECONDS).value().cats;
                if (!userService.checkAdmin(authentication)) {
                    result = result.stream().filter(a -> a.ownerId == userService.getId(authentication)).toList();
                }
                return new ResponseEntity<>(result, HttpStatus.OK);
            } catch (Exception e) {
                System.out.println("!!!Error " + e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }

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
            RequestReplyFuture<String, Long, GetCatDTO> sendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(record);
            try {
                GetCatDTO getCatDTO = sendAndReceive.get(70, TimeUnit.SECONDS).value();
                if (getCatDTO.catDTO == null) {
                    if (userService.checkAdmin(authentication)) {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }

                CatDTO catDTO = getCatDTO.catDTO;
                if (!(userService.checkUserAuthenticated(catDTO.ownerId, authentication)) && !userService.checkAdmin(authentication)) {
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
//        if (!userService.checkAdmin()) {
//            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
//        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            // get cat and check owner
            if (!userService.checkAdmin(authentication)) {
                ProducerRecord<String, Long> record = new ProducerRecord<String, Long>("get_cat_request", id);
                record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_cat_response".getBytes()));
                RequestReplyFuture<String, Long, GetCatDTO> sendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(record);
                try {
                    GetCatDTO getCatDTO = sendAndReceive.get(70, TimeUnit.SECONDS).value();
                    if (getCatDTO.catDTO == null) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }

                    CatDTO catDTO = getCatDTO.catDTO;
                    if (!userService.checkUserAuthenticated(catDTO.ownerId, authentication)) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
                }
            }

            // delete
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
    public CompletableFuture<ResponseEntity<?>> makeFriends(
            @RequestParam(value = "firstCatId") Long firstCatId,
            @RequestParam(value = "secondCatId") Long secondCatId) {
//        catService.makeFriends(firstCatId, secondCatId);
//        catsFriendsRequestKafkaTemplate.send("make_friends", new CatsFriendsRequest(firstCatId, secondCatId));
//        return ResponseEntity.status(HttpStatus.OK).build();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            // check if authenticated user is owner of at least one cats
            if (!userService.checkAdmin(authentication)) {
                ProducerRecord<String, Long> firstRecord = new ProducerRecord<String, Long>("get_cat_request", firstCatId);
                firstRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_cat_response".getBytes()));
                RequestReplyFuture<String, Long, GetCatDTO> firstSendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(firstRecord);

                ProducerRecord<String, Long> secondRecord = new ProducerRecord<String, Long>("get_cat_request", secondCatId);
                secondRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_cat_response".getBytes()));
                RequestReplyFuture<String, Long, GetCatDTO> secondSendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(secondRecord);
                try {
                    GetCatDTO firstGetCatDTO = firstSendAndReceive.get(70, TimeUnit.SECONDS).value();
                    if (firstGetCatDTO.catDTO == null) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    GetCatDTO secondGetCatDTO = secondSendAndReceive.get(70, TimeUnit.SECONDS).value();
                    if (secondGetCatDTO.catDTO == null) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    if (!userService.checkUserAuthenticated(firstGetCatDTO.catDTO.ownerId, authentication) &&
                        !userService.checkUserAuthenticated(secondGetCatDTO.catDTO.ownerId, authentication)) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
                }
            }

            CatsFriendsRequest catsFriendsRequest = new CatsFriendsRequest(firstCatId, secondCatId);
            ProducerRecord<String, CatsFriendsRequest> record = new ProducerRecord<String, CatsFriendsRequest>("make_friends_request", catsFriendsRequest);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "make_friends_response".getBytes()));
            RequestReplyFuture<String, CatsFriendsRequest, CatsFriendsResponse> sendAndReceive = friendsRequestReplyKafkaTemplate.sendAndReceive(record);
            try {
                CatsFriendsResponse response = sendAndReceive.get(70, TimeUnit.SECONDS).value();
                if (response.errorMessage != "") {
                    if (response.errorMessage.contains("not found")) {
                        return new ResponseEntity<>(response.errorMessage, HttpStatus.NOT_FOUND);
                    }
                    return new ResponseEntity<>(response.errorMessage, HttpStatus.BAD_REQUEST);
                }
                return ResponseEntity.status(HttpStatus.OK).build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }

    @PostMapping("/friends/unmake")
    @Operation(summary = "Прекратить дружбу двух котов")
//    @PreAuthorize("hasRole('ROLE_ADMIN') || @catFriendsRequestChecker.check(#firstCatId, #secondCatId)")
    public CompletableFuture<ResponseEntity<?>> unmakeFriends(
            @RequestParam(value = "firstCatId") long firstCatId,
            @RequestParam(value = "secondCatId") long secondCatId) {
//        catsFriendsRequestKafkaTemplate.send("unmake_friends", new CatsFriendsRequest(firstCatId, secondCatId));
//        return ResponseEntity.status(HttpStatus.OK).build();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            // check if authenticated user is owner of at least one cats
            if (!userService.checkAdmin(authentication)) {
                ProducerRecord<String, Long> firstRecord = new ProducerRecord<String, Long>("get_cat_request", firstCatId);
                firstRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_cat_response".getBytes()));
                RequestReplyFuture<String, Long, GetCatDTO> firstSendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(firstRecord);

                ProducerRecord<String, Long> secondRecord = new ProducerRecord<String, Long>("get_cat_request", secondCatId);
                secondRecord.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "get_cat_response".getBytes()));
                RequestReplyFuture<String, Long, GetCatDTO> secondSendAndReceive = getCatReplyingKafkaTemplate.sendAndReceive(secondRecord);
                try {
                    GetCatDTO firstGetCatDTO = firstSendAndReceive.get(70, TimeUnit.SECONDS).value();
                    if (firstGetCatDTO.catDTO == null) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    GetCatDTO secondGetCatDTO = secondSendAndReceive.get(70, TimeUnit.SECONDS).value();
                    if (secondGetCatDTO.catDTO == null) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    if (!userService.checkUserAuthenticated(firstGetCatDTO.catDTO.ownerId, authentication) &&
                            !userService.checkUserAuthenticated(secondGetCatDTO.catDTO.ownerId, authentication)) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
                }
            }

            CatsFriendsRequest catsFriendsRequest = new CatsFriendsRequest(firstCatId, secondCatId);
            ProducerRecord<String, CatsFriendsRequest> record = new ProducerRecord<String, CatsFriendsRequest>("unmake_friends_request", catsFriendsRequest);
            record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "unmake_friends_response".getBytes()));
            RequestReplyFuture<String, CatsFriendsRequest, CatsFriendsResponse> sendAndReceive = friendsRequestReplyKafkaTemplate.sendAndReceive(record);
            try {
                CatsFriendsResponse response = sendAndReceive.get(70, TimeUnit.SECONDS).value();
                if (response.errorMessage != "") {
                    if (response.errorMessage.contains("not found")) {
                        return new ResponseEntity<>(response.errorMessage, HttpStatus.NOT_FOUND);
                    }
                    return new ResponseEntity<>(response.errorMessage, HttpStatus.BAD_REQUEST);
                }
                return ResponseEntity.status(HttpStatus.OK).build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }
}
