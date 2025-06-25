package pa.com.segurossura.logsandaudit.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pa.com.segurossura.logsandaudit.model.entities.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByName(String name);

    //@EntityGraph(attributePaths = {"identificationDocuments"})
    Optional<Person> findById(UUID id);

    @Override
    //@EntityGraph(attributePaths = {"identificationDocuments"})
    List<Person> findAll();

}
