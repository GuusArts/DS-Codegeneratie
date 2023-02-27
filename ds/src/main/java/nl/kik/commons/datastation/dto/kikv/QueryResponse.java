package nl.kik.commons.datastation.dto.kikv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nimbusds.jose.JWSObject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
public class QueryResponse {
	private JWSObject response;
}
