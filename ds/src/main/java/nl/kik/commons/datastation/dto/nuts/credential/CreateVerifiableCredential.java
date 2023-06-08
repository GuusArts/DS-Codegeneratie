package nl.kik.commons.datastation.dto.nuts.credential;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialContexts;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialKeywords;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class CreateVerifiableCredential {
	@JsonProperty("@context")
	private URI context;
	private String type;
	private URI issuer;
	private ZonedDateTime expirationDate;
	private Boolean publishToNetwork;
	private Visibility visibility;
	private CredentialSubject credentialSubject;

	public static CreateVerifiableCredentialBuilder<?, ?> from(VerifiableCredential vc) {
		return CreateVerifiableCredential.builder() //
				.context(guessContext(vc.getContexts()))//
				.type(guessTypes(vc.getTypes())) //
				.credentialSubject(vc.getCredentialSubject()) //
				.expirationDate(vc.getExpirationDate() == null ? null
						: ZonedDateTime.ofInstant(vc.getExpirationDate().toInstant(), ZoneId.of("UTC"))
								.toOffsetDateTime().toZonedDateTime()) //
				.issuer(vc.getIssuer()) //
		;
	}

	private static String guessTypes(List<String> types) {
		if (CollectionUtils.isEmpty(types)) {
			return null;
		}
		if (types.size() == 1) {
			return types.iterator().next();
		}
		types = types.stream() //
				.filter(Predicate.not(VerifiableCredentialKeywords.JSONLD_TERM_VERIFIABLE_CREDENTIAL::equals)) //
				.toList();
		if (types.size() == 1) {
			return types.iterator().next();
		}
		throw new IllegalArgumentException("Cannot guess type from " + types);
	}

	private static URI guessContext(List<URI> contexts) {
		if (CollectionUtils.isEmpty(contexts)) {
			return null;
		}
		if (contexts.size() == 1) {
			return contexts.iterator().next();
		}
		contexts = contexts.stream() //
				.filter(Predicate.not(VerifiableCredentialContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_V1::equals)) //
				.toList();
		if (contexts.size() == 1) {
			return contexts.iterator().next();
		}
		throw new IllegalArgumentException("Cannot guess context from " + contexts);
	}
}
