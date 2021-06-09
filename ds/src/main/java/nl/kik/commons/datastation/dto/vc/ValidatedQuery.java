package nl.kik.commons.datastation.dto.vc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ValidatedQuery extends VerifiableCredential {
	private String subjectId;
	private String profile;
	private String ontology;
	private String query;
}
