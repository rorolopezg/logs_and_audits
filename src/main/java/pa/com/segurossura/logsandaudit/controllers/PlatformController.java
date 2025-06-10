package pa.com.segurossura.logsandaudit.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pa.com.segurossura.logsandaudit.model.entities.TestEntity;
import pa.com.segurossura.logsandaudit.services.IPlatformService;

import java.util.List;

@RestController
@Slf4j
public class PlatformController {
    private IPlatformService platformService;

    public PlatformController(IPlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/test-entities")
    public ResponseEntity<?> getAllTestEntities() {
        log.info("Fetching all test entities");
        List<TestEntity> entityList = platformService.findAllTestEntity();
        log.info("End fetching all test entities");
        return ResponseEntity.ok(entityList);
    }

    @PostMapping("/test-entities")
    public ResponseEntity<?> saveTestEntity(@RequestBody TestEntity testEntity) {
        log.info("Saving test entity: {}", testEntity);
        TestEntity savedEntity = platformService.saveTestEntity(testEntity);
        log.info("End Saving test entity: {}", savedEntity);
        return ResponseEntity.ok(savedEntity);
    }

}
