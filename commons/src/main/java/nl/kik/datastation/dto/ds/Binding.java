package nl.kik.datastation.dto.ds;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode
public class Binding {
	private RDFType type;
	private String value;
	private String language;
	private String datatype;
}
