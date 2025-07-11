package pa.com.segurossura.logsandaudit.config.entitylisteners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
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


@Component
@Slf4j
public class EntityUpdateEventListener implements PostUpdateEventListener {
    private final static String UPDATE = "UPDATE";
    private final ObjectMapper objectMapper;

    public EntityUpdateEventListener(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        // *** SOLUCIÓN A LA RECURSIÓN ***
        // Si la entidad que se está actualizando es un AuditLog, no hacer nada.
        if (event.getEntity() instanceof AuditLog) {
            return;
        }
        EntityPersister persister = event.getPersister();
        String entityName = event.getEntity().getClass().getSimpleName();//persister.getEntityName();
        Serializable id = (Serializable) event.getId();

        Object[] newState = event.getState();
        Object[] oldState = event.getOldState();
        String[] propertyNames = persister.getPropertyNames();

        String user = SecurityUtils.getCurrentUserLogin();
        String userId = SecurityUtils.getCurrentUserObjectId();
        String transactionId = MDC.get(TransactionContextInterceptor.TRANSACTION_ID_KEY);
        String transactionAction = MDC.get(TransactionContextInterceptor.TRANSACTION_ACTION_KEY);


        try {
            MDC.put(LOG_TYPE_KEY, LOG_TYPE_AUDIT);
            MDC.put(TRANSACTION_ACTION_KEY, UPDATE);
            // Si oldState es null, no podemos determinar los cambios específicos de esta manera.
            // Esto ya se manejaba en la versión anterior, lo mantenemos.
            if (oldState == null) {
                String jsonNewStateOnly = convertStateMapToJson(buildStateMap(propertyNames, newState), entityName, id, "NewState");
                log.warn("AUDIT - userId '{}' userName '{}' Update (OldState not available): Type=[{}], ID=[{}], NewState={}",
                        userId,
                        user,
                        entityName,
                        id,
                        jsonNewStateOnly);
                return;
            }

            // Convertir estados completos a JSON
            String jsonOldState = convertStateMapToJson(buildStateMap(propertyNames, oldState), entityName, id, "OldState");
            String jsonNewState = convertStateMapToJson(buildStateMap(propertyNames, newState), entityName, id, "NewState");

            // Identificar y convertir solo las propiedades cambiadas a JSON
            int[] dirtyPropertiesIndices = event.getDirtyProperties();
            String jsonChangedProperties = convertChangedPropertiesToJson(
                    propertyNames,
                    oldState,
                    newState,
                    dirtyPropertiesIndices,
                    entityName,
                    id
            );

            log.info("AUDIT - userId '{}' userName '{}' has updated: Type=[{}], ID=[{}], OldState={}, NewState={}, ChangedProperties={}",
                    userId,
                    user,
                    entityName,
                    id,
                    jsonOldState,
                    jsonNewState,
                    jsonChangedProperties);
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
        if (stateMap.isEmpty() && stateType.equals("OldState")) return "null"; // Para oldState si no había nada
        if (stateMap.isEmpty()) return "{}";


        try {
            return objectMapper.writeValueAsString(stateMap);
        } catch (JsonProcessingException e) {
            log.error("AUDIT - Error converting {} to JSON for Entity: {}, ID: {}", stateType, entityName, id, e);
            return String.format("{\"error\":\"Could not serialize %s to JSON\"}", stateType);
        }
    }

    /**
     * Convierte solo las propiedades que cambiaron a una cadena JSON.
     * El JSON resultante será un objeto donde cada clave es el nombre de la propiedad
     * y su valor es un objeto con las claves "old" y "new".
     * Ejemplo: {"propertyName1": {"old":"valueA", "new":"valueB"}, "propertyName2": ...}
     */
    private String convertChangedPropertiesToJson(String[] propertyNames,
                                                  Object[] oldState,
                                                  Object[] newState,
                                                  int[] dirtyPropertiesIndices,
                                                  String entityName, Serializable id) {
        // oldState ya se verifica que no sea null antes de llamar a esta función.
        if (dirtyPropertiesIndices == null || dirtyPropertiesIndices.length == 0) {
            return "{}"; // Ninguna propiedad marcada como "dirty"
        }

        Map<String, Map<String, Object>> changedProperties = new HashMap<>();
        for (int index : dirtyPropertiesIndices) {
            // Los índices en dirtyPropertiesIndices deben ser válidos para propertyNames, oldState y newState.
            if (index < propertyNames.length && index < oldState.length && index < newState.length) {
                String propertyName = propertyNames[index];
                Object oldValue = oldState[index];
                Object newValue = newState[index];

                if (ObjectUtils.isNullOrEmptyCollection(oldValue) && ObjectUtils.isNullOrEmptyCollection(newValue)) {
                    continue; // Si ambos son null, no hay cambio
                }
                Map<String, Object> diff = new HashMap<>();
                diff.put("old", oldValue);
                diff.put("new", newValue);
                changedProperties.put(propertyName, diff);
            } else {
                log.warn("AUDIT - Index {} from dirtyProperties is out of bounds for entity {} ID {}. Skipping this property.", index, entityName, id);
            }
        }

        if (changedProperties.isEmpty()) {
            return "{}"; // No se pudieron mapear cambios (ej. por índices fuera de rango)
        }

        try {
            return objectMapper.writeValueAsString(changedProperties);
        } catch (JsonProcessingException e) {
            log.error("AUDIT - Error converting changed properties to JSON for Entity: {}, ID: {}", entityName, id, e);
            return "{\"error\":\"Could not serialize changed properties to JSON\"}";
        }
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister entityPersister) {
        return false;
    }
}