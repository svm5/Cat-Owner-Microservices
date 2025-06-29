package labs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Schema(description = "Сущность владельца")
@Getter
public final class OwnerDTO {
    @NotNull
    @Schema(description = "Идентификатор сущности", example = "1")
    public Long id;

    @NotNull
    @Schema(description = "ФИО", example = "Иванов Иван Иванович")
    public String name;

    @NotNull
    @Schema(description = "Дата рождения", example = "2010-01-31")
    public LocalDate birthDate;

    @NotNull
    @Schema(description = "Список идентификаторов котов", example = "[1, 2, 3, 4, 5]")
    public List<Long> cats;

    public OwnerDTO() {}
}
