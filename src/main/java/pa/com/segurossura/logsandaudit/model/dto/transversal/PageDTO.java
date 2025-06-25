package pa.com.segurossura.logsandaudit.model.dto.transversal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Page
 */
@Validated
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonPropertyOrder({
        "content",
        "pageable",
        "last",
        "totalPages",
        "totalElements",
        "size",
        "number",
        "numberOfElements",
        "first",
        "empty",
        "sort"
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("content")
    @Valid
    private List<T> content = null;

    @JsonProperty("pageable")
    private PageableDTO pageable = null;

    @JsonProperty("last")
    private Boolean last = null;

    @JsonProperty("totalPages")
    private Integer totalPages = null;

    @JsonProperty("totalElements")
    private Long totalElements = null;

    @JsonProperty("size")
    private Integer size = null;

    @JsonProperty("number")
    private Integer number = null;

    @JsonProperty("numberOfElements")
    private Integer numberOfElements = null;

    @JsonProperty("first")
    private Boolean first = null;

    @JsonProperty("empty")
    private Boolean empty = null;

    @JsonProperty("sort")
    private SortDTO sort = null;
}
