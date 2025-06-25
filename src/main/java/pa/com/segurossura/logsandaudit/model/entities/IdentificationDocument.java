package pa.com.segurossura.logsandaudit.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "IDENTIFICATION_DOCUMENT", schema = "RLOPEZ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationDocument {
    @Id
    @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @Column(name = "document_type", nullable = false)
    private String documentType;
    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    @JsonIgnore
    private Person person;

    public IdentificationDocument(UUID id, String documentType, String documentNumber) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IdentificationDocument that = (IdentificationDocument) o;
        return Objects.equals(id, that.id) && Objects.equals(documentType, that.documentType) && Objects.equals(documentNumber, that.documentNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentType, documentNumber);
    }
}
