package pa.com.segurossura.logsandaudit.model.mappers;

import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pa.com.segurossura.logsandaudit.model.dto.PartialPersonDTO;
import pa.com.segurossura.logsandaudit.model.entities.Person;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-09T09:46:52-0400",
    comments = "version: 1.6.3, compiler: javac, environment: Java 24.0.1 (Eclipse Adoptium)"
)
@Component
public class PersonMapperImpl implements PersonMapper {

    @Autowired
    private OptionalMapper optionalMapper;

    @Override
    public void patchPersonFromDto(PartialPersonDTO dto, Person person) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getAddress() != null ) {
            person.setAddress( optionalMapper.mapOptionalString( dto.getAddress() ) );
        }
        if ( dto.getBirthDate() != null ) {
            person.setBirthDate( optionalMapper.mapOptionalLocalDate( dto.getBirthDate() ) );
        }
    }
}
