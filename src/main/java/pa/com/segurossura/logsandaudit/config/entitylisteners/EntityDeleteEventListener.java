package pa.com.segurossura.logsandaudit.config.entitylisteners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor;
import pa.com.segurossura.logsandaudit.model.entities.audit.AuditLog;
import pa.com.segurossura.logsandaudit.security.utils.SecurityUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_AUDIT;
import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_KEY;
import static pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor.TRANSACTION_ACTION_KEY;
import static pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor.TRANSACTION_STATUS_KEY;

@Component
@Slf4j
public class EntityDeleteEventListener implements PostDeleteEventListener {
    private final static String DELETE = "DELETE";

    private final ObjectMapper objectMapper;

    public EntityDeleteEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        // *** SOLUCIÓN A LA RECURSIÓN ***
        // Si la entidad que se está eliminando es un AuditLog, no hacer nada.
        if (event.getEntity() instanceof AuditLog) {
            return;
        }
        EntityPersister persister = event.getPersister();
        String user = SecurityUtils.getCurrentUserLogin();
        String userId = SecurityUtils.getCurrentUserObjectId();
        String entityName = event.getEntity().getClass().getSimpleName();//persister.getEntityName();
        Serializable id = (Serializable) event.getId();

        Object[] deletedState = event.getDeletedState();
        String[] propertyNames = persister.getPropertyNames();

        try {
            MDC.put(LOG_TYPE_KEY, LOG_TYPE_AUDIT);
            MDC.put(TRANSACTION_ACTION_KEY, DELETE);
            MDC.put(TRANSACTION_STATUS_KEY, "IN PROGRESS");

            if (deletedState == null) {
                log.warn("AUDIT - userId '{}' userName '{}' Delete (Deleted State not available): Type=[{}], ID=[{}]",
                        userId,
                        user,
                        entityName,
                        id);
                return;
            }

            // Convertir estados completos a JSON
            String jsonDeletedState = convertStateMapToJson(buildStateMap(propertyNames, deletedState), entityName, id, "DeletedState");

            log.info("AUDIT - userId '{}' userName '{}' has deleted: Type=[{}], ID=[{}], DeletedState={}",
                    userId,
                    user,
                    entityName,
                    id,
                    jsonDeletedState);
        } finally {
            MDC.remove(LOG_TYPE_KEY);
            MDC.remove(TRANSACTION_ACTION_KEY);
            MDC.remove(TransactionContextInterceptor.CHANGED_PROPERTY_NAMES_KEY);
            MDC.remove(TransactionContextInterceptor.CHANGED_PROPERTY_VALUES_KEY);
        }
    }

    /**
     * Construye un Map a partir de los nombres de propiedades y sus valores.
     */
    private Map<String, Object> buildStateMap(String[] propertyNames, Object[] stateValues) {
        Map<String, Object> stateMap = new HashMap<>();
        if (stateValues == null) { // Puede ser que oldState sea null
            return stateMap; // Devuelve mapa vacío
        }
        for (int i = 0; i < propertyNames.length; i++) {
            // Asegurarse de que el índice no exceda la longitud del array de valores
            if (i < stateValues.length) {
                stateMap.put(propertyNames[i], stateValues[i]);
            }
        }
        return stateMap;
    }

    /**
     * Convierte un Map de estado (completo o parcial) a una cadena JSON.
     */
    private String convertStateMapToJson(Map<String, Object> stateMap, String entityName, Serializable id, String stateType) {
        if (stateMap.isEmpty() && stateType.equals("DeletedState")) return "null"; // Para deletedState si no había nada
        if (stateMap.isEmpty()) return "{}";

        try {
            return objectMapper.writeValueAsString(stateMap);
        } catch (JsonProcessingException e) {
            log.error("AUDIT - Error converting {} to JSON for Entity: {}, ID: {}", stateType, entityName, id, e);
            return String.format("{\"error\":\"Could not serialize %s to JSON\"}", stateType);
        }
    }


    @Override
    public boolean requiresPostCommitHandling(EntityPersister entityPersister) {
        return false;
    }
}