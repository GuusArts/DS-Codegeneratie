package nl.kik.commons.datastation.dto.didcomm;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString(callSuper = true)
public class Code {
	private Sorter sorter;
	private Scope scope;
	@Singular
	private List<String> descriptors;
	private String otherScope;
}
