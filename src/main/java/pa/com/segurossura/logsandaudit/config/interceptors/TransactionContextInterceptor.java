package pa.com.segurossura.logsandaudit.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TransactionContextInterceptor implements HandlerInterceptor {

    // Define las claves que usarás en el MDC para consistencia
    public static final String TRANSACTION_ID_KEY = "internalTransactionId";
    public static final String TRANSACTION_ACTION_KEY = "internalTransactionAction";
    public static final String TRANSACTION_URI_KEY = "transactionURI";
    public static final String X_TRANSACTION_ID_KEY = "x-transaction-id";
    public static final String X_ORGANIZATION_ID_KEY = "x-organization-id";
    public static final String X_ORGANIZATION_TYPE_KEY = "x-organization-type";
    public static final String X_CLIENT_APPLICATION_KEY = "x-client-application";
    public static final String X_USER_KEY = "x-user";
    public static final String X_CLIENT_APPLICATION_FLOW_KEY = "x-client-application-flow";
    public static final String REMOTE_IP_KEY = "remoteIp";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // Genera un ID de transacción único para cada petición
        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        // Intenta obtener un nombre de acción significativo (ej. "NombreController#nombreMetodo")
        if (handler instanceof HandlerMethod handlerMethod) {
            String actionName = handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName();
            MDC.put(TRANSACTION_ACTION_KEY, actionName);
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

        return true; // Continúa con la ejecución de la cadena de interceptores y el controller
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // Limpia el MDC después de que la petición se ha completado
        // Es crucial para evitar que los datos de esta petición se filtren a la siguiente en el mismo hilo.
        MDC.remove(TRANSACTION_ID_KEY);
        MDC.remove(TRANSACTION_ACTION_KEY);
        MDC.remove(TRANSACTION_URI_KEY);
        MDC.remove(X_TRANSACTION_ID_KEY);
        MDC.remove(X_ORGANIZATION_ID_KEY);
        MDC.remove(X_ORGANIZATION_TYPE_KEY);
        MDC.remove(X_CLIENT_APPLICATION_KEY);
        MDC.remove(X_USER_KEY);
        MDC.remove(X_CLIENT_APPLICATION_FLOW_KEY);
        MDC.remove(REMOTE_IP_KEY);
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