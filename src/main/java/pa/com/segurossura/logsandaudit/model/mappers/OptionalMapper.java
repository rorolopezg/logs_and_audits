package pa.com.segurossura.logsandaudit.model.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class OptionalMapper {

    @Named("mapOptionalString")
    public String mapOptionalString(Optional<String> value) {
        return value.orElse(null);
    }

    @Named("mapOptionalLocalDate")
    public LocalDate mapOptionalLocalDate(Optional<LocalDate> value) {
        return value.orElse(null);
    }
}
