package pa.com.segurossura.logsandaudit.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TransactionContextInterceptor transactionContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Añade tu interceptor para que se aplique a todas las peticiones a la API
        registry.addInterceptor(transactionContextInterceptor).addPathPatterns("/test-entities/**", "/test-entities-paged/**"); // Ajusta el patrón de URL según tu estructura
    }
}