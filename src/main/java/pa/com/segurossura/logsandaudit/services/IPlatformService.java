package pa.com.segurossura.logsandaudit.services;

import pa.com.segurossura.logsandaudit.model.entities.TestEntity;
import pa.com.segurossura.logsandaudit.repositories.ITestEntityRepository;

import java.util.List;

public interface IPlatformService {
    List<TestEntity> findAllTestEntity();
    TestEntity saveTestEntity(TestEntity testEntity);
}
