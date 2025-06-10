package pa.com.segurossura.logsandaudit.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pa.com.segurossura.logsandaudit.model.entities.TestEntity;
import pa.com.segurossura.logsandaudit.repositories.ITestEntityRepository;

import java.util.List;

@Service
public class PlatformServiceImpl implements IPlatformService {
    private final ITestEntityRepository testJpaRepository;

    public PlatformServiceImpl(ITestEntityRepository testJpaRepository) {
        this.testJpaRepository = testJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestEntity> findAllTestEntity() {
        return testJpaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = false)
    public TestEntity saveTestEntity(TestEntity testEntity) {
        return testJpaRepository.save(testEntity);
    }
}
