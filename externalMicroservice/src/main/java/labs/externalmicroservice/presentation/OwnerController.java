package labs.externalmicroservice.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import labs.*;
import labs.externalmicroservice.service.OwnerService;
import labs.externalmicroservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/owners")
@Tag(name = "Owners", description = "Управление владельцами")
@SecurityRequirement(name = "basicAuth")
public class OwnerController {
    @Autowired
    private UserService userService;

    @Autowired
    OwnerService ownerService;

    @GetMapping("/{id}")
    @Operation(summary = "Получение владельца по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", description = "Owner not found", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<OwnerDTO>> getOwner(@PathVariable Long id) {
        if (!userService.checkUserAuthenticated(id) && !userService.checkAdmin()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                GetOwnerDTO result = ownerService.getOwner(id);
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
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<List<OwnerDTO>>> getAllOwners(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "birthday", required = false) String birthday,
            @RequestParam(name = "page", defaultValue = "0") String page,
            @RequestParam(name = "size", defaultValue = "3") String size) {
        if (!userService.checkAdmin()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        }

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
            try {
                return new ResponseEntity<>(ownerService.getAllOwners(getAllOwnersRequest).owners, HttpStatus.OK);
            } catch (Exception e) {
                System.out.println("!!!Error " + e.getMessage());
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
        });
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление конкретного владельца")
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) })
    })
    public CompletableFuture<ResponseEntity<Object>> deleteOwner(@PathVariable Long id) {
        if (!userService.checkUserAuthenticated(id) && !userService.checkAdmin()) {
            return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
        }
        return CompletableFuture.supplyAsync(() -> {
                try {
                    // Отправляем и ждем подтверждения
                    ownerService.deleteOwnerById(id);
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
    @Operation(summary = "Удаление всех владельцев")
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "403", description = "Cannot access", content = { @Content(schema = @Schema()) })
    })
    public ResponseEntity<Void> deleteAllOwners() {
        if (!userService.checkAdmin()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        ownerService.deleteAllOwners();

        return ResponseEntity.noContent().build();
    }
}
