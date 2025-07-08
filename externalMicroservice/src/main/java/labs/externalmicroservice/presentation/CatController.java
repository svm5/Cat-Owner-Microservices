package labs.externalmicroservice.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import labs.*;
import labs.externalmicroservice.service.CatService;
import labs.externalmicroservice.service.OwnerService;
import labs.externalmicroservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/cats")
@Tag(name = "Cats", description = "Управление котами")
@SecurityRequirement(name = "basicAuth")
public class CatController {
    @Autowired
    private CatService catService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Получение всех котов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    public CompletableFuture<ResponseEntity<List<CatDTO>>> getAllCats(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "birthday", required = false) String birthday,
            @RequestParam(name = "breed", required = false) String breed,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "eyesColor", required = false) String eyesColor,
            @RequestParam(name = "page", defaultValue = "0") String page,
            @RequestParam(name = "size", defaultValue = "3") String size) {
        System.out.println("GET ALL CATS " + SecurityContextHolder.getContext().getAuthentication());
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
        SecurityContext context = SecurityContextHolder.getContext();
        CompletableFuture<ResponseEntity<List<CatDTO>>> res = CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(context);
            try {
                GetAllCatsResponse getAllCatsResponse = catService.getAllCats(getAllCatsRequest);
                List<CatDTO> result = getAllCatsResponse.cats;
                if (!userService.checkAdmin(authentication)) {
                    result = result.stream().filter(a -> a.ownerId == userService.getId(authentication)).toList();
                }
                return ResponseEntity.status(HttpStatus.OK).body(result);
            } catch (Exception e) {
                System.out.println("!!!Error " + e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
        return res;
    }

    @PostMapping(consumes = {"application/json"})
    @Operation(summary = "Создание кота")
    @ApiResponses({
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Cat was not found", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<CatDTO>> createCat(@RequestBody CreateCatDTO createCatDTO) {
        if (!userService.checkUserAuthenticated(createCatDTO.owner_id) && !userService.checkAdmin()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                GetOwnerDTO result = ownerService.getOwner(createCatDTO.owner_id);
                if (result.ownerDTO == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }

            try {
                CatDTO catDTO = catService.createCat(createCatDTO);
                return new ResponseEntity<>(catDTO, HttpStatus.CREATED);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение кота по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Cat was not found", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<CatDTO>> getCat(@PathVariable("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            try {
                GetCatDTO getCatDTO = catService.getCatById(id);
                if (getCatDTO.catDTO == null) {
                    if (userService.checkAdmin(authentication)) {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }

                CatDTO catDTO = getCatDTO.catDTO;
                if (!(userService.checkUserAuthenticated(catDTO.ownerId, authentication))
                        && !userService.checkAdmin(authentication)) {
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
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Cat was not found", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<Object>> deleteCat(@PathVariable long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            if (!userService.checkAdmin(authentication)) {
                try {
                    GetCatDTO getCatDTO = catService.getCatById(id);
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

            try {
                catService.deleteCatById(id);

                return ResponseEntity.noContent().build();
            } catch (Exception e) {
                throw new RuntimeException("Error", e);
            }
        })
        .exceptionally(ex -> {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        });
    }

    @DeleteMapping
    @Operation(summary = "Удаление всех котов")
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Cat was not found", content = { @Content(schema = @Schema()) })
    })
    public ResponseEntity<Void> deleteAllCats() {
        if (!userService.checkAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catService.deleteAllCats();

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/friends/make")
    @Operation(summary = "Подружить двух котов")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = { @Content(schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Cat was not found", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<?>> makeFriends(
            @RequestParam(value = "firstCatId") Long firstCatId,
            @RequestParam(value = "secondCatId") Long secondCatId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            // check if authenticated user is owner of at least one cats
            if (!userService.checkAdmin(authentication)) {
                try {
                    GetCatDTO firstGetCatDTO = catService.getCatById(firstCatId);
                    GetCatDTO secondGetCatDTO = catService.getCatById(secondCatId);
                    if (firstGetCatDTO.catDTO == null || secondGetCatDTO.catDTO == null) {
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
            try {
                boolean makeFriends = true;
                CatsFriendsResponse response = catService.makeFriends(catsFriendsRequest, makeFriends);
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = { @Content(schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Cat was not found", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<?>> unmakeFriends(
            @RequestParam(value = "firstCatId") long firstCatId,
            @RequestParam(value = "secondCatId") long secondCatId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            // check if authenticated user is owner of at least one cats
            if (!userService.checkAdmin(authentication)) {
                try {
                    GetCatDTO firstGetCatDTO = catService.getCatById(firstCatId);
                    GetCatDTO secondGetCatDTO = catService.getCatById(secondCatId);

                    if (firstGetCatDTO.catDTO == null || secondGetCatDTO.catDTO == null) {
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
            try {
                boolean makeFriends = false;
                CatsFriendsResponse response = catService.makeFriends(catsFriendsRequest, false);
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

    @PatchMapping("change/owner")
    @Operation(summary = "Смена хозяина кота")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = { @Content(schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Cat was not found", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<?>> changeOwner(
            @RequestParam(value = "catId") Long catId,
            @RequestParam(value = "newOwnerId") Long newOwnerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return CompletableFuture.supplyAsync(() -> {
            try {
                GetCatDTO getCatDTO = catService.getCatById(catId);
                if (getCatDTO.catDTO == null) {
                    if (userService.checkAdmin(authentication)) {
                        return new ResponseEntity<>(getCatDTO.error, HttpStatus.NOT_FOUND);
                    }
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
                if (!userService.checkAdmin(authentication) && !userService.checkUserAuthenticated(getCatDTO.catDTO.ownerId, authentication)) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }

            // change owner
            try {
                ChangeOwnerResponse response = catService.changeOwner(new ChangeOwnerRequest(catId, newOwnerId));
                if (response.errorMessage != "") {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.errorMessage);
                }
                return ResponseEntity.status(HttpStatus.OK).build();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }
}
