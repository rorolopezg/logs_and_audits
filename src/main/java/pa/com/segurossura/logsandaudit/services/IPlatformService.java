package pa.com.segurossura.logsandaudit.services;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.data.domain.Page;
import pa.com.segurossura.logsandaudit.model.dto.PartialPersonDTO;
import pa.com.segurossura.logsandaudit.model.dto.PersonDTO;
import pa.com.segurossura.logsandaudit.model.dto.transversal.PageDTO;
import pa.com.segurossura.logsandaudit.model.entities.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPlatformService {
    public Optional<PersonDTO> findPersonById(UUID id);

    List<PersonDTO> findAllPerson();

    public PageDTO findAllPersonPaged(Integer pageNumber, Integer pageSize);

    public Page<Person> findAllPersonPaged2(Integer pageNumber, Integer pageSize);

    PersonDTO createPerson(PersonDTO personDTO);

    PersonDTO updatePerson(UUID id, PersonDTO personDTO) throws JsonMappingException;

    PersonDTO patchPerson(UUID id, PartialPersonDTO personDTO);
}
