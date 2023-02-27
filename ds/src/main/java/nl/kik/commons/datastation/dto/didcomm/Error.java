package nl.kik.commons.datastation.dto.didcomm;

import java.util.List;
import java.util.UUID;

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
import nl.kik.commons.datastation.json.ListOrSingle;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error extends Message<ProblemReport> {
	@Singular("singleAck")
	@JsonSerialize(using = ListOrSingle.Serialize.class)
	@JsonDeserialize(using = ListOrSingle.DeserializeUUID.class)
	private List<UUID> ack;
}
