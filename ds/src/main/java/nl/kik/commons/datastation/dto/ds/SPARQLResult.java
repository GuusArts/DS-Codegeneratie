package nl.kik.commons.datastation.dto.ds;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class SPARQLResult {
	private Header head;
}
