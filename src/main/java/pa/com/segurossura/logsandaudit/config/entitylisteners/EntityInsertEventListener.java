package pa.com.segurossura.logsandaudit.config.entitylisteners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import pa.com.segurossura.logsandaudit.model.entities.audit.AuditLog;
import pa.com.segurossura.logsandaudit.security.utils.SecurityUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_AUDIT;
import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_KEY;
import static pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor.*;

@Component
@Slf4j
public class EntityInsertEventListener implements PostInsertEventListener {
    private final static String INSERT = "INSERT";

    private final ObjectMapper objectMapper;

    public EntityInsertEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        // *** SOLUCIÓN A LA RECURSIÓN ***
        // Si la entidad que se está insertando es un AuditLog, no hacer nada.
        if (event.getEntity() instanceof AuditLog) {
            return;
        }
        EntityPersister persister = event.getPersister();
        String user = SecurityUtils.getCurrentUserLogin();
        String userId = SecurityUtils.getCurrentUserObjectId();
        String entityName = event.getEntity().getClass().getSimpleName();//persister.getEntityName();
        Serializable id = (Serializable) event.getId();

        Object[] insertedState = event.getState();
        String[] propertyNames = persister.getPropertyNames();

        try {
            MDC.put(LOG_TYPE_KEY, LOG_TYPE_AUDIT);
            MDC.put(TRANSACTION_ACTION_KEY, INSERT);
            if (insertedState == null) {
                log.warn("AUDIT - userId '{}' userName '{}' Insert (Inserted State not available): Type=[{}], ID=[{}]",
                        userId,
                        user,
                        entityName,
                        id);
                return;
            }

            // Convertir estados completos a JSON
            String jsonInsertedState = convertStateMapToJson(buildStateMap(propertyNames, insertedState), entityName, id, "InsertedState");

            log.info("AUDIT - userId '{}' userName '{}' has inserted: Type=[{}], ID=[{}], InsertedState={}",
                    userId,
                    user,
                    entityName,
                    id,
                    jsonInsertedState);
        } finally {
            MDC.remove(LOG_TYPE_KEY);
            MDC.remove(TRANSACTION_ACTION_KEY);
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
        if (stateMap.isEmpty() && stateType.equals("InsertedState")) return "null"; // Para deletedState si no había nada
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