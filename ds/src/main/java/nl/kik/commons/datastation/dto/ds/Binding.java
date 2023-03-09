package nl.kik.commons.datastation.dto.ds;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class Binding {
	private RDFType type;
	private String value;
	private String language;
	private String datatype;
}
