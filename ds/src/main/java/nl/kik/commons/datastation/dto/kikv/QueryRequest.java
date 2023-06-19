package nl.kik.commons.datastation.dto.kikv;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nl.kik.commons.datastation.dto.kikv.credential.ValidatedQueryCredential;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
import nl.kik.commons.datastation.json.Base64EncodedJSONLD;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryRequest {
	@JsonSerialize(using = Base64EncodedJSONLD.Serialize.class)
	@JsonDeserialize(using = Base64EncodedJSONLD.Deserialize.class)
	private VerifiablePresentation vp;
	private CredentialSubject credentialSubject;
	private String param_values;

	@JsonIgnore
	public Collection<CredentialSubject> getCredentialSubjects() {
		if (vp != null) {
			return CollectionUtils.emptyIfNull(vp.getVerifiableCredentials()).stream() //
					.map(VerifiableCredential::getCredentialSubject) //
					.toList();
		}
		if (credentialSubject == null) {
			return List.of();
		}
		return List.of(credentialSubject);
	}

	public CredentialSubject getCredentialSubject() {
		Collection<CredentialSubject> vcs = getCredentialSubjects();
		if (CollectionUtils.isEmpty(vcs)) {
			return null;
		}
		if (vcs.size() > 1) {
			return null; // Cannot use simplified mode with >1 subject
		}
		return vcs.iterator().next();
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
