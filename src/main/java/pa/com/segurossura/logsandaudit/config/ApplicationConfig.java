package pa.com.segurossura.logsandaudit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig { // Puede ser una clase de configuración general o tu SecurityConfig
    @Bean
    //@Primary // Opcional: Marca este ObjectMapper como el principal si hay otros definidos.
    // Es útil para asegurar que tu configuración personalizada sea la que se inyecte por defecto.
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());

        // --- Personalizaciones Comunes ---

        // Para manejar correctamente los tipos de Java 8 Date/Time (LocalDate, LocalDateTime, etc.)
        objectMapper.registerModule(new JavaTimeModule());

        // Para que las fechas no se escriban como timestamps numéricos, sino como strings (ej. ISO-8601)
        // Esto es común junto con JavaTimeModule.
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Para ignorar propiedades desconocidas durante la deserialización (útil si el JSON tiene más campos)
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Para que el JSON de salida sea "bonito" (indentado). Útil para debugging, no recomendado para producción.
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Hibernate6Module hibernate6Module = new Hibernate6Module();
        // Opciones comunes para el módulo de Hibernate:
        // Por defecto, no fuerza la carga de asociaciones lazy. Las serializará como null
        // si no están inicializadas, o solo sus IDs si están configuradas de esa manera.
        // hibernate6Module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false); // false es usualmente el default y preferido
        // Para evitar serializar el campo 'hibernateLazyInitializer' explícitamente:
        hibernate6Module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        // Esta última opción es muy útil: si un proxy lazy no está cargado, Jackson serializará su ID en lugar de intentar serializar el proxy o fallar.

        objectMapper.registerModule(hibernate6Module);


        // Aquí puedes añadir cualquier otra configuración que necesites:
        // - Formateadores de fecha personalizados
        // - MixIns
        // - Serializadores/Deserializadores personalizados

        return objectMapper;
    }

}