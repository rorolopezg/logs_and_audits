package pa.com.segurossura.logsandaudit.model.dto;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import pa.com.segurossura.logsandaudit.model.entities.Person;

import java.sql.Types;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentificationDocumentDTO {
    private UUID id;
    private String documentType;
    private String documentNumber;
}
