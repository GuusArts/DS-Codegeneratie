package nl.kik.commons.datastation.dto.ds;

import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class SelectBody {
	@Singular(ignoreNullCollections = true)
	private List<Map<String, Binding>> bindings;
}
