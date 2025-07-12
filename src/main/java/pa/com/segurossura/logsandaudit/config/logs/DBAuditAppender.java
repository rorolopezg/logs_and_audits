package pa.com.segurossura.logsandaudit.config.logs;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor;
import pa.com.segurossura.logsandaudit.model.entities.audit.AuditLog;
import pa.com.segurossura.logsandaudit.services.audit.AuditLogService;
import pa.com.segurossura.logsandaudit.security.utils.SecurityUtils;

import java.time.Instant;
import java.util.Map;

public class DBAuditAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private AuditLogService auditLogService;
    private boolean contextInitialized = false;

    @Override
    protected void append(ILoggingEvent event) {
        // Si el contexto de Spring no está listo, simplemente ignora el log.
        // Esto evita errores durante el arranque de la aplicación.
        if (ApplicationContextProvider.getContext() == null) {
            return;
        }

        // Intenta obtener el bean de servicio solo una vez de forma segura.
        if (!contextInitialized) {
            try {
                this.auditLogService = ApplicationContextProvider.getContext().getBean(AuditLogService.class);
                contextInitialized = true;
            } catch (Exception e) {
                addError("No se pudo obtener el bean AuditLogService. El appender de BD no funcionará.", e);
                return; // No continuar si no se puede obtener el servicio.
            }
        }

        // Si el servicio no está disponible, no hacer nada.
        if (auditLogService == null) {
            return;
        }

        try {
            Map<String, String> mdc = event.getMDCPropertyMap();
            String stackTrace = null;
            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if (throwableProxy != null) {
                stackTrace = ThrowableProxyUtil.asString(throwableProxy);
            }
            String username = mdc.get(TransactionContextInterceptor.USERNAME_KEY);
            String userId = mdc.get(TransactionContextInterceptor.USER_ID_KEY);
            AuditLog auditLog = AuditLog.builder()
                    .traceId(mdc.get("traceId")) // Obtener traceId del MDC
                    .spanId(mdc.get("spanId"))   // Obtener spanId del MDC
                    .timestamp(Instant.ofEpochMilli(event.getTimeStamp()))
                    .level(event.getLevel().toString())
                    .loggerName(event.getLoggerName())
                    .threadName(event.getThreadName())
                    .lastLogin(mdc.get(TransactionContextInterceptor.USER_LAST_LOGIN_KEY) == null ? null : Instant.parse(mdc.get(TransactionContextInterceptor.USER_LAST_LOGIN_KEY)))
                    .userId(userId)
                    .username(username)
                    .rolIds(null)
                    .rolNames(mdc.get(TransactionContextInterceptor.ROLESNAMES_KEY))
                    .internalTransactionId(mdc.get(TransactionContextInterceptor.TRANSACTION_ID_KEY))
                    .externalTransactionId(mdc.get(TransactionContextInterceptor.X_TRANSACTION_ID_KEY))
                    .transactionTimestamp(mdc.get(TransactionContextInterceptor.TRANSACTION_TIMESTAMP_KEY) == null ? null : Instant.parse(mdc.get(TransactionContextInterceptor.TRANSACTION_TIMESTAMP_KEY)))
                    .transactionName(mdc.get(TransactionContextInterceptor.TRANSACTION_NAME_KEY))
                    .transactionAction(mdc.get(TransactionContextInterceptor.TRANSACTION_ACTION_KEY))
                    .remoteIp(mdc.get(TransactionContextInterceptor.REMOTE_IP_KEY))
                    .message(event.getFormattedMessage())
                    .status(mdc.get(TransactionContextInterceptor.TRANSACTION_STATUS_KEY) == null ? "UNKNOWN" : mdc.get(TransactionContextInterceptor.TRANSACTION_STATUS_KEY))
                    .stackTrace(stackTrace)
                    .b2cTenantId(mdc.get(TransactionContextInterceptor.B2C_TENANT_ID_KEY))
                    .idpTenantId(mdc.get(TransactionContextInterceptor.IDP_TENANT_ID_KEY))
                    .requestUri(mdc.get(TransactionContextInterceptor.TRANSACTION_URI_KEY))
                    .changedPropertyNames(mdc.get(TransactionContextInterceptor.CHANGED_PROPERTY_NAMES_KEY))
                    .changedPropertyValues(mdc.get(TransactionContextInterceptor.CHANGED_PROPERTY_VALUES_KEY))
                    .build();

            auditLogService.saveLog(auditLog);

        } catch (Exception e) {
            addError("Error al escribir el log en la base de datos.", e);
        }
    }
}