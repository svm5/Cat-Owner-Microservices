package labs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Schema(description = "Сущность кота")
public final class CatDTO {
    @NotNull
    @Schema(description = "Идентификатор сущности", example = "100")
    public long id;

    @NotNull
    @Schema(description = "Имя", example = "Мурка")
    public String name;

    @NotNull
    @Schema(description = "Дата рождения", example = "2018-05-29")
    public LocalDate birthDate;

    @NotNull
    @Schema(description = "Порода", example = "Персидская")
    public String breedName;

    @NotNull
    @Schema(description = "Окрас", example = "WHITE")
    public CatColors color;

    @NotNull
    @Schema(description = "Идентификатор владельца", example = "1")
    public long ownerId;

    @NotNull
    @Schema(description = "Цвет глаз", example = "blue")
    public String eyesColor;

    @NotNull
    @Schema(description = "Список идентификаторв друзей", example = "[2, 3, 100]")
    public List<Long> friends;

    public CatDTO() {}
}
