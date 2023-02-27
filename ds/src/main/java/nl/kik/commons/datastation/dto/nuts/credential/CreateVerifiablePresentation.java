package nl.kik.commons.datastation.dto.nuts.credential;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateVerifiablePresentation {
	@Singular
	private List<VerifiableCredential> verifiableCredentials;
	private URI signerDID;
	private ProofPurpose proofPurpose;
	private String challenge;
	private String domain;
	private ZonedDateTime expires;
}
