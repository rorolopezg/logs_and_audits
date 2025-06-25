package pa.com.segurossura.logsandaudit.model.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import pa.com.segurossura.logsandaudit.model.entities.IdentificationDocument;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDTO {
    private UUID id;
    private String name;
    private String address;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private Set<IdentificationDocumentDTO> identificationDocuments;

}
