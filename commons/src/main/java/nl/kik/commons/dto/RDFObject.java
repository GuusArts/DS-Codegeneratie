package nl.kik.commons.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode
public class RDFObject {
	@Setter
	@EqualsAndHashCode.Exclude
	private String id;
}
