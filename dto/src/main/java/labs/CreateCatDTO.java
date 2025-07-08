package labs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Данные для создания кота")
public final class CreateCatDTO {
    @NotNull
    @Schema(description = "Имя", example = "Мурка")
    public String name;

    @NotNull
    @Schema(description = "Дата рождения", example = "2018-05-29")
    public LocalDate birthday;

    @NotNull
    @Schema(description = "Порода", example = "Персидская")
    public String breed;

    @NotNull
    @Schema(description = "Окрас", example = "WHITE")
    public CatColors color;

    @NotNull
    @Schema(description = "Цвет глаз", example = "green")
    public String eyesColor;

    @NotNull
    @Schema(description = "Id владельца", example = "1")
    public Long owner_id;

    public CreateCatDTO() { }

    public CreateCatDTO(String name, LocalDate birthday, String breed, CatColors color, String eyesColor, Long owner_id) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }
        if (birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("birthday must not be greater than now");
        }

        this.name = name;
        this.birthday = birthday;
        this.breed = breed;
        this.color = color;
        this.eyesColor = eyesColor;
        this.owner_id = owner_id;
    }
}
