package pa.com.segurossura.logsandaudit.services;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.com.segurossura.logsandaudit.model.dto.PartialPersonDTO;
import pa.com.segurossura.logsandaudit.model.dto.PersonDTO;
import pa.com.segurossura.logsandaudit.model.dto.transversal.PageDTO;
import pa.com.segurossura.logsandaudit.model.dto.transversal.PageableDTO;
import pa.com.segurossura.logsandaudit.model.entities.Person;
import pa.com.segurossura.logsandaudit.model.mappers.PersonMapper;
import pa.com.segurossura.logsandaudit.repositories.IPersonRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlatformServiceImpl implements IPlatformService {
    private final IPersonRepository testRepository;
    private final ObjectMapper objectMapper;
    private final PersonMapper personMapper;

    public PlatformServiceImpl(
            IPersonRepository testRepository,
            ObjectMapper objectMapper,
            PersonMapper personMapper
    ) {
        this.testRepository = testRepository;
        this.objectMapper = objectMapper;
        this.personMapper = personMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonDTO> findPersonById(UUID id) {
        Optional<PersonDTO> result = testRepository.findById(id).map(person -> {
            try {
                person.getIdentificationDocuments().size(); // Force loading of identificationDocuments
                return objectMapper.convertValue(person, PersonDTO.class);
            } catch (Exception e) {
                throw new RuntimeException("Error converting Person to PersonDTO", e);
            }
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonDTO> findAllPerson() {
        List<PersonDTO> result = testRepository.findAll().stream().map(person -> {
            try {
                return objectMapper.convertValue(person, PersonDTO.class);
            } catch (Exception e) {
                throw new RuntimeException("Error converting Person to PersonDTO", e);
            }
        }).toList();
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO findAllPersonPaged(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        Page<Person> personPage = testRepository.findAll(pageable);
        PageDTO<PersonDTO> result;

        result = objectMapper.convertValue(personPage, PageDTO.class);
        /*
        result.setContent(personPage.getContent().stream()
                .map(person -> objectMapper.convertValue(person, PersonDTO.class))
                .toList());

        result.setPageable( objectMapper.convertValue(personPage.getPageable(), PageableDTO.class) );
        */
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Person> findAllPersonPaged2(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
        Page<Person> personPage = testRepository.findAll(pageable);

        return personPage;
    }

    @Override
    @Transactional(readOnly = false)
    public PersonDTO createPerson(PersonDTO personDTO) {
        if (personDTO.getId() != null) {
            throw new IllegalArgumentException("PersonDTO id must be null in create operation");
        }
        testRepository.findByName(personDTO.getName())
                .ifPresent(existingEntity -> {
                    throw new RuntimeException("Person with id " + personDTO.getName() + " already exists");
                });
        Person person = objectMapper.convertValue(personDTO, Person.class);
        person.getIdentificationDocuments().forEach(document -> {
            document.setPerson(person); // Set the person reference in each document
        });
        testRepository.save(person);
        PersonDTO savedEntityDTO = objectMapper.convertValue(person, PersonDTO.class);
        return savedEntityDTO;
    }

    @Override
    @Transactional(readOnly = false)
    public PersonDTO updatePerson(UUID id, PersonDTO personDTO) throws JsonMappingException {
        if (id == null) {
            throw new IllegalArgumentException("PersonDTO id must not be null in update operation");
        }
        if (personDTO.getId() != null && !personDTO.getId().equals(id)) {
            throw new IllegalArgumentException("PersonDTO id does not match the path variable id");
        }
        Person existingEntity = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person with id " + personDTO.getId() + " does not exist"));

        objectMapper.updateValue(existingEntity, personDTO);
        existingEntity.setId(id); // Ensure the ID is set correctly
        existingEntity.getIdentificationDocuments().forEach(document -> {
            document.setPerson(existingEntity); // Set the person reference in each document
        });
        testRepository.save(existingEntity);
        PersonDTO updatedEntityDTO = objectMapper.convertValue(existingEntity, PersonDTO.class);

        return updatedEntityDTO;
    }

    @Override
    public PersonDTO patchPerson(UUID id, PartialPersonDTO personDTO) {
        Person existingEntity = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person with id " + id + " does not exist"));

        personMapper.patchPersonFromDto(personDTO, existingEntity);
        existingEntity = testRepository.save(existingEntity);

        PersonDTO existingEntityDTO = objectMapper.convertValue(existingEntity, PersonDTO.class);
        return existingEntityDTO;
    }
}
