package pa.com.segurossura.logsandaudit.model.dto.transversal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableDTO implements Serializable {
    private Integer pageNumber;
    private Integer pageSize;
    private SortDTO sort;
    private Long offset;
    private Boolean paged;
    private Boolean unpaged;
}
