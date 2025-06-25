package pa.com.segurossura.logsandaudit.config;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addCustomModules() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.modulesToInstall(new Jdk8Module());
    }
}
