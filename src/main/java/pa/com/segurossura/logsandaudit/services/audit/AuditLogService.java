package pa.com.segurossura.logsandaudit.services.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pa.com.segurossura.logsandaudit.model.entities.audit.AuditLog;
import pa.com.segurossura.logsandaudit.repositories.IAuditLogRepository;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final IAuditLogRepository auditLogRepository;

    /**
     * Guarda una entrada de log en la base de datos.
     * Se ejecuta en una nueva transacción para asegurar que el log se guarde
     * incluso si la transacción principal de la aplicación hace rollback.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }
}