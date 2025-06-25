package pa.com.segurossura.logsandaudit.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Optional;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartialPersonDTO {
    private Optional<String> address;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Optional<LocalDate> birthDate;
}
