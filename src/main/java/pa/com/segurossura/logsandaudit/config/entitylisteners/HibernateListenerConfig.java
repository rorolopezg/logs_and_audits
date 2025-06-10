package pa.com.segurossura.logsandaudit.config.entitylisteners;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateListenerConfig {
    public static final String LOG_TYPE_KEY = "log_type"; // Clave para el MDC
    public static final String LOG_TYPE_APP = "APPLICATION";
    public static final String LOG_TYPE_AUDIT = "AUDIT";
    private final EntityManagerFactory entityManagerFactory;
    private final EntityDeleteEventListener entityDeleteEventListener; // Inyecta tu listener si lo defines como un Bean
    private final EntityInsertEventListener entityInsertEventListener; // Inyecta tu listener si lo defines como un Bean
    private final EntityUpdateEventListener entityUpdateEventListener; // Inyecta tu listener si lo defines como un Bean

    public HibernateListenerConfig(
            EntityManagerFactory entityManagerFactory,
            EntityDeleteEventListener entityDeleteEventListener,
            EntityInsertEventListener entityInsertEventListener,
            EntityUpdateEventListener entityUpdateEventListener) {
        this.entityManagerFactory = entityManagerFactory;
        this.entityDeleteEventListener = entityDeleteEventListener;
        this.entityInsertEventListener = entityInsertEventListener;
        this.entityUpdateEventListener = entityUpdateEventListener;
    }

    @PostConstruct
    public void registerListeners() {
        // Obtenemos la SessionFactory de Hibernate a partir del EntityManagerFactory
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        // Registramos el listener para los eventos que nos interesan
        // Puedes pasar una instancia nueva o una gestionada por Spring
        registry.appendListeners(EventType.POST_DELETE, entityDeleteEventListener);
        registry.appendListeners(EventType.POST_INSERT, entityInsertEventListener);
        registry.appendListeners(EventType.POST_UPDATE, entityUpdateEventListener);
    }
}