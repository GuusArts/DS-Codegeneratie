package nl.kik.commons.datastation.dto.didcomm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nl.kik.commons.datastation.json.CodeSerialization;
import nl.kik.commons.datastation.json.ListOrSingle;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemReport {
	@JsonSerialize(using = CodeSerialization.Serialize.class)
	@JsonDeserialize(using = CodeSerialization.Deserialize.class)
	private Code code;
	private String comment;
	@Singular
	@JsonSerialize(using = ListOrSingle.Serialize.class)
	@JsonDeserialize(using = ListOrSingle.DeserializeString.class)
	private List<String> args;
	private String escalate_to;
}
