package pa.com.segurossura.logsandaudit.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "PERSON", schema = "RLOPEZ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "address", nullable = true)
    private String address;
    @Column(name = "birth_date", nullable = true)
    private LocalDate birthDate;
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = false)
    private Set<IdentificationDocument> identificationDocuments;

    public Person(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Person(UUID id, String name, String address, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.birthDate = birthDate;
    }

    // method to add an identification document
    public void addIdentificationDocument(IdentificationDocument document) {
        if (identificationDocuments != null) {
            identificationDocuments.add(document);
            document.setPerson(this);
        }
    }

    // method to remove an identification document
    public void removeIdentificationDocument(IdentificationDocument document) {
        if (identificationDocuments != null) {
            identificationDocuments.remove(document);
            document.setPerson(null);
        }
    }

    // method to clear identification documents
    public void clearIdentificationDocuments() {
        if (identificationDocuments != null) {
            for (IdentificationDocument document : identificationDocuments) {
                document.setPerson(null);
            }
            identificationDocuments.clear();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
