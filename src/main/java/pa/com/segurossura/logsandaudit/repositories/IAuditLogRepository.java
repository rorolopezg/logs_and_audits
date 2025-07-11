package pa.com.segurossura.logsandaudit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pa.com.segurossura.logsandaudit.model.entities.audit.AuditLog;

@Repository
public interface IAuditLogRepository extends JpaRepository<AuditLog, Long> {
}