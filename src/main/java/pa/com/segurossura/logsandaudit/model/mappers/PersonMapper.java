package pa.com.segurossura.logsandaudit.model.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pa.com.segurossura.logsandaudit.model.dto.PartialPersonDTO;
import pa.com.segurossura.logsandaudit.model.dto.PersonDTO;
import pa.com.segurossura.logsandaudit.model.entities.Person;

import java.time.LocalDate;
import java.util.Optional;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {OptionalMapper.class} // Esto le dice a MapStruct que use OptionalMapper para las conversiones
)
public interface PersonMapper {

    // Mapea sólo campos no nulos desde el DTO hacia la entidad existente
    @Mapping(target = "address", qualifiedByName = "mapOptionalString")
    @Mapping(target = "birthDate", qualifiedByName = "mapOptionalLocalDate")
    void patchPersonFromDto(PartialPersonDTO dto, @MappingTarget Person person);
    /*
    void updatePersonFromDto(PersonDTO dto, @MappingTarget Person person);
    PersonDTO toDto(Person person);
    PartialPersonDTO toPartialDto(Person person);
    Person toEntity(PersonDTO personDTO);
    Person toEntity(PartialPersonDTO personDTO);
    */
}