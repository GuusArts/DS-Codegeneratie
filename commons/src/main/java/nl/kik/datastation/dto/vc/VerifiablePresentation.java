package nl.kik.datastation.dto.vc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VerifiablePresentation extends VerifiableBase {
	private VerifiableCredential credential;
}
