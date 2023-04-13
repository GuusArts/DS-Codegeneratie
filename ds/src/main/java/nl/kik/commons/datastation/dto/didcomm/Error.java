package nl.kik.commons.datastation.dto.didcomm;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;
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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error extends Message<ProblemReport> {
	@Singular(value = "singleAck", ignoreNullCollections = true)
	@JsonFormat(with = { Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
	private List<UUID> ack;
}
