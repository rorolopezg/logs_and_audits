package pa.com.segurossura.logsandaudit.model.entities.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AUDIT_LOGS") // O el nombre que prefieras para tu tabla
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_log_seq")
    @SequenceGenerator(name = "audit_log_seq", sequenceName = "AUDIT_LOG_ID_SEQ", allocationSize = 1)
    private Long id;

    // NUEVO: Columna para el Trace ID de Micrometer
    @Column(name = "trace_id")
    private String traceId;

    // NUEVO: Columna para el Span ID de Micrometer
    @Column(name = "span_id")
    private String spanId;

    @Column(name = "log_timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "transaction_timestamp")
    private Instant transactionTimestamp;

    @Column(name = "log_level", length = 10)
    private String level;

    @Column(name = "logger_name", length = 255)
    private String loggerName;

    @Column(name = "thread_name", length = 100)
    private String threadName;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "userId", length = 100)
    private String userId;

    @Column(name = "username", length = 128)
    private String username;

    @Column(name = "rolIds", length = 32)
    private String rolIds;

    @Column(name = "rolNames", length = 2048)
    private String rolNames;

    @Column(name = "internal_transaction_id", length = 50)
    private String internalTransactionId;

    @Column(name = "external_transaction_id", length = 50)
    private String externalTransactionId;

    @Column(name = "transaction_name", length = 255)
    private String transactionName;

    @Column(name = "transaction_action", length = 255)
    private String transactionAction;

    @Column(name = "remote_ip", length = 50)
    private String remoteIp;

    @Lob // Para mensajes largos
    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "status", length = 32)
    private String status;

    @Lob // Para stack traces largos
    @Column(name = "stack_trace")
    private String stackTrace;

    @Column(name = "b2c_tenant_id")
    private String b2cTenantId;

    @Column(name = "idp_tenant_id")
    private String idpTenantId;
}
