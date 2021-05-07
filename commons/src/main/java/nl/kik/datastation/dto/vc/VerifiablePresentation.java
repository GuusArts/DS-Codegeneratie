package nl.kik.datastation.dto.vc;

import com.nimbusds.jose.JWSObject;

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
	private JWSObject externalCredential;
}
