package nl.kik.commons.datastation.dto.nuts.oauth;

import java.net.URI;
import java.util.List;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
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
public class CreateJwtGrant {
	private URI authorizer;
	private URI requester;
	private VerifiablePresentation identity;
	private String service;
	@Singular(ignoreNullCollections = true)
	private List<VerifiableCredential> credentials;
}
