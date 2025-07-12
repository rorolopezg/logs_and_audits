package pa.com.segurossura.logsandaudit.config.entitylisteners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
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
import static pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor.*;

@Component
@Slf4j
public class EntityInsertEventListener implements PostInsertEventListener {
    private final static String INSERT = "INSERT";
    private final static int MAX_JSON_LENGTH = 3999 ; // Limite de longitud para el JSON

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
            MDC.put(TRANSACTION_STATUS_KEY, "IN PROGRESS");
            if (insertedState == null) {
                log.warn("AUDIT - userId '{}' userName '{}' Insert (Inserted State not available): Type=[{}], ID=[{}]",
                        userId,
                        user,
                        entityName,
                        id);
                return;
            }

            // Obtenemos el JSON con la lista de nombres de las propiedades insertadas
            String jsonPropertyNames = getInsertedPropertyNamesAsJson(propertyNames, entityName, id);
            // Convertir estados completos a JSON
            String jsonInsertedState = convertStateMapToJson(buildStateMap(propertyNames, insertedState), entityName, id, "InsertedState");

            MDC.put(TransactionContextInterceptor.CHANGED_PROPERTY_NAMES_KEY, entityName + ": " + jsonPropertyNames);
            MDC.put(TransactionContextInterceptor.CHANGED_PROPERTY_VALUES_KEY, jsonInsertedState);

            log.info("AUDIT - userId '{}' userName '{}' has inserted: Type=[{}], ID=[{}], InsertedState={}",
                    userId,
                    user,
                    entityName,
                    id,
                    jsonInsertedState);
        } finally {
            MDC.remove(LOG_TYPE_KEY);
            MDC.remove(TRANSACTION_ACTION_KEY);
            MDC.remove(TransactionContextInterceptor.CHANGED_PROPERTY_NAMES_KEY);
            MDC.remove(TransactionContextInterceptor.CHANGED_PROPERTY_VALUES_KEY);
        }
    }

    /**
     * Toma el array de nombres de propiedades de una entidad y lo convierte en una
     * cadena de texto con formato de array JSON.
     *
     * @param propertyNames Array con todos los nombres de las propiedades de la entidad.
     * @param entityName Nombre de la entidad (para logging de errores).
     * @param id ID de la entidad (para logging de errores).
     * @return Un String con formato JSON como ["id", "name", "address"] o un array vacío "[]".
     */
    private String getInsertedPropertyNamesAsJson(String[] propertyNames, String entityName, Serializable id) {
        if (propertyNames == null || propertyNames.length == 0) {
            return "[]"; // Devuelve un array JSON vacío
        }

        try {
            // Usa el ObjectMapper para serializar el array de strings a un array JSON
            return objectMapper.writeValueAsString(propertyNames);
        } catch (JsonProcessingException e) {
            log.error("AUDIT - Error converting inserted property names to JSON for Entity: {}, ID: {}", entityName, id, e);
            return "[\"Error serializing property names\"]";
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
            String result = objectMapper.writeValueAsString(stateMap);
            return result.substring(0, Math.min(result.length(), MAX_JSON_LENGTH));
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