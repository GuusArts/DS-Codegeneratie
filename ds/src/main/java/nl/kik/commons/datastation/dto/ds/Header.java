package nl.kik.commons.datastation.dto.ds;

import java.net.URI;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode
public class Header {
	@Singular(ignoreNullCollections = true)
	private List<String> vars;
	@Singular(value = "singleLink", ignoreNullCollections = true)
	private List<URI> link;
}
