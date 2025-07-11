package pa.com.segurossura.logsandaudit.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

public class LoggingJwtDecoder implements JwtDecoder {

    private final JwtDecoder delegate;
    private final Logger log = LoggerFactory.getLogger(LoggingJwtDecoder.class);

    public LoggingJwtDecoder(JwtDecoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Intenta decodificar y validar el token usando el decodificador real
            return delegate.decode(token);
        } catch (JwtException e) {
            // Si ocurre cualquier error de validación, lo registramos en el log
            log.warn("Error en la validación del token JWT: {}", e.getMessage());

            // Re-lanzamos la excepción para que Spring Security la maneje y devuelva el 401
            throw e;
        }
    }
}