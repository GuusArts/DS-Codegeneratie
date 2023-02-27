package nl.kik.commons.datastation.dto.kikv;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nl.kik.commons.datastation.json.Base64EncodedJSONLD;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryRequest {
	@JsonSerialize(using = Base64EncodedJSONLD.Serialize.class)
	@JsonDeserialize(using = Base64EncodedJSONLD.Deserialize.class)
	private VerifiablePresentation vp;
	private String param_values;
}
