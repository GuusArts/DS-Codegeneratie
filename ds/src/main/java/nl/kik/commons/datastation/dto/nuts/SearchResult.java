package nl.kik.commons.datastation.dto.nuts;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult {
	@Singular
    private List<VerifiableCredentialSearchResult> verifiableCredentials;

}
