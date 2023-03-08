package nl.kik.commons.datastation.dto.kikv;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryRequest {
	@JsonIgnore
//	@JsonSerialize(using = Base64EncodedJSONLD.Serialize.class)
//	@JsonDeserialize(using = Base64EncodedJSONLD.Deserialize.class)
	private VerifiablePresentation vp;
	private CredentialSubject credentialSubject;
	private String param_values;

	public CredentialSubject getCredentialSubject() {
		if (credentialSubject != null) {
			return credentialSubject;
		}
		if (vp == null)
			return null;
		VerifiableCredential credential = vp.getVerifiableCredential();
		if (credential == null)
			return null;
		return credential.getCredentialSubject();
	}
	
	public VerifiablePresentation getVp() {
		if (vp != null) {
			return vp;
		}
		if (credentialSubject == null) {
			return null;
		}
		return VerifiablePresentation.builder() //
				.verifiableCredential(ValidatedQueryCredential.builder() //
						.credentialSubject(credentialSubject) //
						.build()) //
				.build();
	}

}
