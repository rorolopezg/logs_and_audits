package pa.com.segurossura.logsandaudit.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pa.com.segurossura.logsandaudit.model.dto.PartialPersonDTO;
import pa.com.segurossura.logsandaudit.model.dto.PersonDTO;
import pa.com.segurossura.logsandaudit.model.dto.transversal.PageDTO;
import pa.com.segurossura.logsandaudit.services.IPlatformService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class PlatformController {
    private IPlatformService platformService;

    public PlatformController(IPlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/test-entities")
    public ResponseEntity<?> findAllPerson() {
        log.info("Fetching all person");
        List<PersonDTO> entityList = platformService.findAllPerson();
        log.info("End fetching all person, found: {}", entityList.size());
        return ResponseEntity.ok(entityList);
    }

    @GetMapping("/test-entities/{id}")
    public ResponseEntity<?> findPersonById(@PathVariable UUID id) {
        log.info("Fetching person by id: {}", id);
        Optional<PersonDTO> peronOptional = platformService.findPersonById(id);
        log.info("End fetching person by id: {}, found: {}", id, peronOptional.isPresent());
        return ResponseEntity.ok(peronOptional.orElseThrow(() -> new RuntimeException("Person with id " + id + " does not exist")));
    }

    @GetMapping("/test-entities-paged")
    public ResponseEntity<?> findAllPersonPaged(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) {
        log.info("Fetching all person with pagination, pageNumber: {}, pageSize: {}", pageNumber, pageSize);
        PageDTO<PersonDTO> entityList = platformService.findAllPersonPaged(pageNumber, pageSize);
        log.info("End fetching all person, found: {}", entityList.getSize());
        return ResponseEntity.ok(entityList);
    }

    @GetMapping("/test-entities-paged2")
    public ResponseEntity<?> findAllPersonPaged2(@RequestParam Integer pageNumber, @RequestParam Integer pageSize) {
        return ResponseEntity.ok(platformService.findAllPersonPaged2(pageNumber, pageSize));
    }

    @PostMapping("/test-entities")
    public ResponseEntity<?> createPerson(@RequestBody PersonDTO personDTO) {
        log.info("Creating person: {}", personDTO);
        PersonDTO savedEntity = platformService.createPerson(personDTO);
        log.info("End Creating person: {}", savedEntity);
        return ResponseEntity.ok(savedEntity);
    }

    @PutMapping("/test-entities/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable UUID id, @RequestBody PersonDTO personDTO) throws JsonMappingException {
        log.info("Updating person: {}", personDTO);
        PersonDTO savedEntity = platformService.updatePerson(id, personDTO);
        log.info("End Updating person: {}", savedEntity);
        return ResponseEntity.ok(savedEntity);
    }

    @PatchMapping("/test-entities/{id}")
    public ResponseEntity<?> patchPerson(@PathVariable UUID id, @RequestBody PartialPersonDTO personDTO) throws JsonMappingException {
        log.info("Patching person: {}", personDTO);
        PersonDTO savedEntity = platformService.patchPerson(id, personDTO);
        log.info("End Patching person: {}", savedEntity);
        return ResponseEntity.ok(savedEntity);
    }
}
