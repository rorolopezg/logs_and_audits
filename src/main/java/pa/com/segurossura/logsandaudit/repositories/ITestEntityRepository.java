package pa.com.segurossura.logsandaudit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pa.com.segurossura.logsandaudit.model.entities.TestEntity;

import java.util.UUID;

public interface ITestEntityRepository extends JpaRepository<TestEntity, UUID> {

    // Aquí puedes agregar métodos personalizados si es necesario
    // Por ejemplo, para buscar por nombre:
    // List<TestEntity> findByName(String name);
}
