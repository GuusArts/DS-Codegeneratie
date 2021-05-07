package nl.kik.datastation.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.vc.ValidatedQuery;
import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.dto.vc.VerifiableCredential;
import nl.kik.datastation.dto.vc.VerifiablePresentation;
import nl.kik.datastation.util.FunctionWrapper.BiFunctionWithException;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

public class VerifiableCredentialService extends AbstractTokenService {
	protected static final String VP = "vp";
	protected static final String VC = "vc";
	protected static final String CONTEXT = "@context";
	protected static final String TYPE = "type";
	protected static final String VERIFIABLE_CREDENTIAL = "verifiableCredential";
	protected static final String CREDENTIAL_SUBJECT = "credentialSubject";
	protected static final String PROFILE = "profile";
	protected static final String ONTOLOGY = "ontology";
	protected static final String QUERY = "query";

	protected static final String CREDENTIALS_CONTEXT = "https://www.w3.org/2018/credentials/v1";
	protected static final String VALIDATED_QUERY_CONTEXT = "https://www.zinl.nl/2020/credentials/validatedquery/v1";
	protected static final String VERIFIABLE_PRESENTATION_TYPE = "VerifiablePresentation";
	protected static final String VERIFIABLE_CREDENTIAL_TYPE = "VerifiableCredential";
	protected static final String VALIDATED_QUERY_CREDENTIAL_TYPE = "VerifiablePresentation";

	public <E extends Exception> JWSObject wrap(VerifiableBase m,
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		m = fillDefaults(m);
		JWSHeader header = new JWSHeader(JWSAlgorithm.EdDSA, JOSEObjectType.JWT, null, null, null, null, null, null,
				null, null, m.getKeyId(), true, null, null);

		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder() //
				.jwtID(m.getId()) //
				.issuer(m.getFrom()) //
				.audience(m.getTo()) //
				.notBeforeTime(m.getValidFrom() == null ? null : Date.from(m.getValidFrom().toInstant())) //
				.expirationTime(m.getExpiration() == null ? null : Date.from(m.getExpiration().toInstant())) //
				.issueTime(m.getCreation() == null ? null : Date.from(m.getCreation().toInstant())) //
		;
		claims = wrap(claims, m, credentialSigner);

		return new SignedJWT(header, claims.build());
	}

	private <E extends Exception> Builder wrap(Builder claims, VerifiableBase m,
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		if (m instanceof VerifiablePresentation) {
			return wrap(claims, (VerifiablePresentation) m, credentialSigner);
		}
		if (m instanceof VerifiableCredential) {
			return wrap(claims, (VerifiableCredential) m);
		}
		return wrapExtension(claims, m, credentialSigner);
	}

	protected <E extends Exception> Builder wrapExtension(Builder claims, VerifiableBase m,
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		throw new IllegalArgumentException(
				"Received unexpected subclass of VerifiableBase (" + m.getClass().getCanonicalName() + ")");
	}

	private <E extends Exception> Builder wrap(Builder claims, VerifiablePresentation m,
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		return claims //
				.claim(VP, makePresentation(m, credentialSigner)) //
		;
	}

	private <E extends Exception> JSONObject makePresentation(VerifiablePresentation m,
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		VCBuilder builder = VCBuilder.builder() //
				.context(CREDENTIALS_CONTEXT) //
				.type(VERIFIABLE_PRESENTATION_TYPE) //
				.claim(VERIFIABLE_CREDENTIAL, credentialSigner
						.apply(m.getCredential(), wrap(m.getCredential(), credentialSigner)).serialize()) //
		;
		builder = makePresentationExtension(builder, m, credentialSigner);
		return builder.build();
	}

	protected <E extends Exception> VCBuilder makePresentationExtension(VCBuilder builder, VerifiablePresentation m,
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		return builder;
	}

	private Builder wrap(Builder claims, VerifiableCredential m) {
		return claims //
				.claim(VC, makeCredential(m)) //
		;
	}

	private JSONObject makeCredential(VerifiableCredential m) {
		VCBuilder builder = VCBuilder.builder() //
				.context(CREDENTIALS_CONTEXT) //
				.type(VERIFIABLE_CREDENTIAL_TYPE) //
		;
		if (m instanceof ValidatedQuery) {
			builder = makeCrendential(builder, (ValidatedQuery) m);
		} else {
			builder = makeCredentialExtension(builder, m);
		}
		return builder.build();
	}

	private VCBuilder makeCrendential(VCBuilder builder, ValidatedQuery m) {
		builder = builder //
				.context(VALIDATED_QUERY_CONTEXT) //
				.type(VALIDATED_QUERY_CREDENTIAL_TYPE) //
				.claim(CREDENTIAL_SUBJECT, new JSONObject(Map.of(//
						PROFILE, m.getProfile(), //
						ONTOLOGY, m.getOntology(), //
						QUERY, m.getQuery() //
				))); //
		builder = makeCredentialExtension(builder, m);
		return builder;
	}

	protected VCBuilder makeCredentialExtension(VCBuilder builder, VerifiableCredential m) {
		return builder;
	}

	protected VCBuilder makeCredentialExtension(VCBuilder builder, ValidatedQuery m) {
		return builder;
	}

	public <T extends VerifiableBase, E extends Exception> FunctionWithException<T, JWSObject, Exception> wrapAndSign(
			JWSSigner signer, BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) {
		FunctionWithException<JWSObject, JWSObject, JOSEException> s = sign(signer);
		return vc -> s.apply(wrap(vc, credentialSigner));
	}

	public FunctionWithException<JWSObject, JWSObject, JOSEException> sign(JWSSigner signer) {
		return vc -> {
			vc.sign(signer);
			return vc;
		};
	}

	@SuppressWarnings("unchecked")
	private <T extends VerifiableBase> T fillDefaults(T m) {
		return (T) m.toBuilder() //
				.id(m.getId() == null ? randomUUID() : m.getId()) //
				.creation(m.getCreation() == null ? OffsetDateTime.now().toZonedDateTime() : m.getCreation()) //
				.validFrom(m.getValidFrom() == null ? OffsetDateTime.now().toZonedDateTime() : m.getValidFrom()) //
				.build();
	}

	public static class VCBuilder {
		private JSONObject result;
		private List<String> context;
		private List<String> type;

		public static VCBuilder builder() {
			return new VCBuilder();
		}

		private VCBuilder() {
			result = new JSONObject();
			context = new ArrayList<>();
			type = new ArrayList<>();
			result.put(CONTEXT, context);
			result.put(TYPE, type);
		}

		public JSONObject build() {
			return result;
		}

		public VCBuilder context(List<String> cs) {
			this.context.removeAll(cs);
			this.context.addAll(cs);
			return this;
		}

		public VCBuilder context(String... cs) {
			return context(Arrays.asList(cs));
		}

		public VCBuilder type(List<String> ts) {
			this.type.removeAll(ts);
			this.type.addAll(ts);
			return this;
		}

		public VCBuilder type(String... ts) {
			return type(Arrays.asList(ts));
		}

		public VCBuilder claim(String key, Object value) {
			this.result.put(key, value);
			return this;
		}
	}

}
