package pa.com.segurossura.logsandaudit.config.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Arrays;

import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_AUDIT;
import static pa.com.segurossura.logsandaudit.config.entitylisteners.HibernateListenerConfig.LOG_TYPE_KEY;
import static pa.com.segurossura.logsandaudit.config.interceptors.TransactionContextInterceptor.*;

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
        BusinessException error = null;
        ResponseEntity<BusinessException> responseEntity;
        if (ex instanceof AccessDeniedException) {
            suggestion = "El usuario no cuenta con los permisos necesarios para ejecutar la operación. Consulte con su administrador.";
            error = BusinessException.builder()
                    .suggestion(suggestion)
                    .status(HttpStatus.FORBIDDEN)
                    .timestamp(LocalDateTime.now())
                    .message(ex.getLocalizedMessage())
                    .trace(null)
                    .path(request.getRequestURI())
                    .requestId(request.getHeader("x-transaction-id"))
                    .documentationUrl("")
                    .build();
            error.setStackTrace(new StackTraceElement[0]);
            responseEntity = new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        } else if (ex instanceof BusinessException) {
            error = (BusinessException) ex;
            responseEntity = new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
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
            responseEntity = new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            MDC.put(LOG_TYPE_KEY, LOG_TYPE_AUDIT);
            log.info("AUDIT - Operation failed: ID={}, Action={},URI={}, Cause={}, DTO Object={}",
                    MDC.get(TRANSACTION_ID_KEY),
                    MDC.get(TRANSACTION_ACTION_KEY),
                    MDC.get(TRANSACTION_URI_KEY),
                    error.getLocalizedMessage(),
                    error.getDtoObject() != null ? error.getDtoObject().toString() : "null"
                    );
        } finally {
            MDC.remove(LOG_TYPE_KEY);
        }

        return responseEntity;
    }
}