package pa.com.segurossura.logsandaudit.model.dto.transversal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Sort
 */
@Validated
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SortDTO {
    @JsonProperty("empty")
    private Boolean empty = null;
    @JsonProperty("sorted")
    private Boolean sorted = null;
    @JsonProperty("unsorted")
    private Boolean unsorted = null;
}
