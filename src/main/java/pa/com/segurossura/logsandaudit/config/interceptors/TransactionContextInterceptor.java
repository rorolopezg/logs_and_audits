package pa.com.segurossura.logsandaudit.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import pa.com.segurossura.logsandaudit.security.utils.SecurityUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_AUDIT;
import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_KEY;

@Component
@Slf4j
public class TransactionContextInterceptor implements HandlerInterceptor {

    // Define las claves que usarás en el MDC para consistencia
    public static final String TRANSACTION_ID_KEY = "internalTransactionId";
    public static final String TRANSACTION_NAME_KEY = "internalTransactionName";
    public static final String TRANSACTION_ACTION_KEY = "internalTransactionAction";
    public static final String TRANSACTION_URI_KEY = "transactionURI";
    public static final String TRANSACTION_STATUS_KEY = "transactionStatus";
    public static final String TRANSACTION_TIMESTAMP_KEY = "transactionTimestamp";
    public static final String X_TRANSACTION_ID_KEY = "x-transaction-id";
    public static final String X_ORGANIZATION_ID_KEY = "x-organization-id";
    public static final String X_ORGANIZATION_TYPE_KEY = "x-organization-type";
    public static final String X_CLIENT_APPLICATION_KEY = "x-client-application";
    public static final String X_USER_KEY = "x-user";
    public static final String X_CLIENT_APPLICATION_FLOW_KEY = "x-client-application-flow";
    public static final String REMOTE_IP_KEY = "remoteIp";
    public static final String USER_LAST_LOGIN_KEY = "userLastLogin";
    public static final String USERNAME_KEY = "username";
    public static final String USER_ID_KEY = "userId";
    public static final String ROLESNAMES_KEY = "rolesNames";
    public static final String ROLESIDS_KEY = "rolesIds";
    public static final String B2C_TENANT_ID_KEY = "b2cTenantId";
    public static final String IDP_TENANT_ID_KEY = "idpTenantId";
    public static final String CHANGED_PROPERTY_NAMES_KEY = "changedPropertyNames";
    public static final String CHANGED_PROPERTY_VALUES_KEY = "changedPropertyValues";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // Genera un ID de transacción único para cada petición
        try {
            String transactionId = UUID.randomUUID().toString();
            String username = SecurityUtils.getCurrentUserLogin();
            String userId = SecurityUtils.getCurrentUserObjectId();
            String rolesNames = SecurityUtils.getRolesFromAuthentication().stream().collect(Collectors.joining(","));

            MDC.put(LOG_TYPE_KEY, LOG_TYPE_AUDIT);
            MDC.put(TRANSACTION_STATUS_KEY, "STARTED");
            MDC.put(B2C_TENANT_ID_KEY, SecurityUtils.getB2cTenantId());
            MDC.put(IDP_TENANT_ID_KEY, SecurityUtils.getIdpTenantId());
            MDC.put(TRANSACTION_ID_KEY, transactionId);
            MDC.put(USERNAME_KEY, username);
            MDC.put(USER_ID_KEY, userId);
            MDC.put(ROLESNAMES_KEY, rolesNames);
            // Intenta obtener un nombre de acción significativo (ej. "NombreController#nombreMetodo")
            if (handler instanceof HandlerMethod handlerMethod) {
                String transactionName = handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName();
                MDC.put(TRANSACTION_NAME_KEY, transactionName);
            }

            // Usa el método y la URI de la petición
            MDC.put(TRANSACTION_URI_KEY, request.getMethod() + ":" + request.getRequestURI());
            MDC.put(X_TRANSACTION_ID_KEY, request.getHeader(X_TRANSACTION_ID_KEY));
            MDC.put(X_ORGANIZATION_ID_KEY, request.getHeader(X_ORGANIZATION_ID_KEY));
            MDC.put(X_ORGANIZATION_TYPE_KEY, request.getHeader(X_ORGANIZATION_TYPE_KEY));
            MDC.put(X_CLIENT_APPLICATION_KEY, request.getHeader(X_CLIENT_APPLICATION_KEY));
            MDC.put(X_USER_KEY, request.getHeader(X_USER_KEY));
            MDC.put(X_CLIENT_APPLICATION_FLOW_KEY, request.getHeader(X_CLIENT_APPLICATION_FLOW_KEY));
            MDC.put(REMOTE_IP_KEY, getRemoteIp(request));
            MDC.put(TRANSACTION_TIMESTAMP_KEY, Instant.now().toString());

            log.info("AUDIT - Transaction started: ID={}, Transaction Name={}, URI={}",
                    transactionId,
                    MDC.get(TRANSACTION_NAME_KEY),
                    MDC.get(TRANSACTION_URI_KEY));
        } finally {
            MDC.remove(LOG_TYPE_KEY);
        }

        return true; // Continúa con la ejecución de la cadena de interceptores y el controller
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        try {
            MDC.put(LOG_TYPE_KEY, LOG_TYPE_AUDIT);
            if (MDC.get(TRANSACTION_STATUS_KEY).equals("FAILED"))
                MDC.put(TRANSACTION_STATUS_KEY, "COMPLETED (FAILED)");
            else
                MDC.put(TRANSACTION_STATUS_KEY, "COMPLETED (SUCCESS)");

            log.info("AUDIT - Transaction completed: ID={}, Transaction Name={}, URI={}",
                    MDC.get(TRANSACTION_ID_KEY),
                    MDC.get(TRANSACTION_NAME_KEY),
                    MDC.get(TRANSACTION_URI_KEY));
        } finally {
            // Limpia el MDC después de que la petición se ha completado
            // Es crucial para evitar que los datos de esta petición se filtren a la siguiente en el mismo hilo.
            MDC.remove(LOG_TYPE_KEY);
            MDC.remove(TRANSACTION_ID_KEY);
            MDC.remove(TRANSACTION_NAME_KEY);
            MDC.remove(TRANSACTION_ACTION_KEY);
            MDC.remove(TRANSACTION_URI_KEY);
            MDC.remove(TRANSACTION_STATUS_KEY);
            MDC.remove(TRANSACTION_TIMESTAMP_KEY);
            MDC.remove(X_TRANSACTION_ID_KEY);
            MDC.remove(X_ORGANIZATION_ID_KEY);
            MDC.remove(X_ORGANIZATION_TYPE_KEY);
            MDC.remove(X_CLIENT_APPLICATION_KEY);
            MDC.remove(X_USER_KEY);
            MDC.remove(X_CLIENT_APPLICATION_FLOW_KEY);
            MDC.remove(REMOTE_IP_KEY);
            MDC.remove(USER_LAST_LOGIN_KEY);
            MDC.remove(USERNAME_KEY);
            MDC.remove(USER_ID_KEY);
            MDC.remove(ROLESNAMES_KEY);
            MDC.remove(ROLESIDS_KEY);
            MDC.remove(B2C_TENANT_ID_KEY);
            MDC.remove(IDP_TENANT_ID_KEY);
            MDC.remove(CHANGED_PROPERTY_NAMES_KEY);
            MDC.remove(CHANGED_PROPERTY_VALUES_KEY);
        }
    }

    private String getRemoteIp(HttpServletRequest request) {
        String xffHeader = request.getHeader("X-Forwarded-For");
        if (xffHeader == null || xffHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // La cabecera X-Forwarded-For puede contener una lista de IPs si hay múltiples proxies.
        // La IP original del cliente es siempre la primera (la de más a la izquierda).
        return xffHeader.split(",")[0].trim();
    }
}