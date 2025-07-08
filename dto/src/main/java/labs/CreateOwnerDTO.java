package labs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Данные для создания владельца")
public final class CreateOwnerDTO {
    public Long id;

    @NotNull
    @Schema(description = "ФИО", example = "Иванов Иван Иванович")
    public String name;

    @NotNull
    @Schema(description = "Дата рождения", example = "2010-01-31")
    public LocalDate birthday;

    public CreateOwnerDTO() {  }

    public CreateOwnerDTO(final String name, final LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("birthday must be greater than now");
        }

        this.name = name;
        this.birthday = birthday;
    }

    public CreateOwnerDTO(long id, final String name, final LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("birthday must be greater than now");
        }

        this.id = id;
        this.name = name;
        this.birthday = birthday;
    }
}
