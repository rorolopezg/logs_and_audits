package pa.com.segurossura.logsandaudit.config.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Arrays;

@ControllerAdvice
public class ExceptionControllerAdvice {
    @SuppressWarnings("unused")
    private static final Logger log = (Logger) LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    @Autowired
    private HttpServletRequest request;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BusinessException> exceptionHandler(Exception ex) {
        log.error("Exception occurred:", ex);
        String suggestion = "";
        BusinessException error;
        if (!(ex instanceof BusinessException)) {
            suggestion = "Ocurrió un error inesperado. Por favor, inténtelo de nuevo más tarde o contáctese con el administrador.";
            error = BusinessException.builder()
                    .suggestion(suggestion)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .timestamp(LocalDateTime.now())
                    .message(ex.getLocalizedMessage())
                    .trace(ex.getCause() != null ? ex.getCause().getMessage() : "")
                    .path(request.getRequestURI())
                    .requestId(request.getHeader("x-transaction-id"))
                    .documentationUrl("")
                    .build();
        } else {
            error = (BusinessException) ex;
        }

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}