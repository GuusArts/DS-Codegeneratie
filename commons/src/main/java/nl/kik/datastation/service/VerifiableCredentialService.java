package nl.kik.datastation.service;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.vc.ValidatedQuery;
import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.dto.vc.VerifiableCredential;
import nl.kik.datastation.dto.vc.VerifiablePresentation;
import nl.kik.datastation.util.FunctionWrapper.BiConsumerWithException;
import nl.kik.datastation.util.FunctionWrapper.BiFunctionWithException;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

@Slf4j
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
	protected static final String ID = "id";

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
		JWSObject credential = m.getExternalCredential();
		if (credential == null) {
			credential = credentialSigner.apply(m.getCredential(), wrap(m.getCredential(), credentialSigner));
		}
		VCBuilder builder = VCBuilder.builder() //
				.context(CREDENTIALS_CONTEXT) //
				.type(VERIFIABLE_PRESENTATION_TYPE) //
				.claim(VERIFIABLE_CREDENTIAL, credential.serialize()) //
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
						ID, m.getSubjectId() == null ? randomUUID() : m.getSubjectId(), //
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

	public <T extends VerifiableBase, E extends Exception, F extends Exception> FunctionWithException<T, JWSObject, Exception> wrapAndSign(
			FunctionWithException<T, JWSSigner, F> signer,
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) {
		return vc -> sign(signer.apply(vc)).apply(wrap(vc, credentialSigner));
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

	public <E extends Exception> VerifiableBase unwrapVerifiable(String encoded,
			BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator)
			throws E, ParseException, MalformedURLException {
		SignedJWT object = SignedJWT.parse(encoded);
		JWSHeader header = object.getHeader();
		JWTClaimsSet claims = object.getJWTClaimsSet();
		checkEquals("jti", JOSEObjectType.JWT, header.getType());
		checkEquals("alg", JWSAlgorithm.EdDSA, header.getAlgorithm());

		VerifiableBase result = unwrap(claims, credentialValidator) //
				.id(claims.getJWTID()) //
				.keyId(header.getKeyID()) //
				.from(claims.getIssuer()) //
				.to(claims.getAudience()) //
				.expiration(claims.getExpirationTime() == null ? null
						: claims.getExpirationTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.creation(claims.getIssueTime() == null ? null
						: claims.getIssueTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.validFrom(claims.getNotBeforeTime() == null ? null
						: claims.getNotBeforeTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.build();
		credentialValidator.accept(object, result);
		return result;
	}

	private <E extends Exception> VerifiableBase.VerifiableBaseBuilder<?, ?> unwrap(JWTClaimsSet claims,
			BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator)
			throws ParseException, MalformedURLException, E {
		JSONObject vp = claims.getJSONObjectClaim(VP);
		JSONObject vc = claims.getJSONObjectClaim(VC);
		if (!(vp == null ^ vc == null)) {
			throw new ParseException("Exactly one of vc or vp must be given", 0);
		}
		if (vp != null) {
			return unwrapPresentation(vp, credentialValidator);
		}
		if (vc != null) {
			return unwrapCredential(vc, credentialValidator);
		}
		throw new IllegalArgumentException("It should not be possible to reach this code");
	}

	private <E extends Exception> VerifiablePresentation.VerifiablePresentationBuilder<?, ?> unwrapPresentation(
			JSONObject vp, BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator)
			throws ParseException, MalformedURLException, E {
		Set<String> types = getTypes(vp);
		Set<String> contexts = getContexts(vp);
		boolean vpType = !types.remove(VERIFIABLE_PRESENTATION_TYPE);
		boolean vpContext = !contexts.remove(CREDENTIALS_CONTEXT);
		if (vpType && vpContext) {
			throw new ParseException("Expecting VC to contain type " + VERIFIABLE_PRESENTATION_TYPE + " and context "
					+ CREDENTIALS_CONTEXT, 0);
		}
		return VerifiablePresentation.builder() //
				.credential(
						requireType(unwrapVerifiable(getRequiredString(vp, VERIFIABLE_CREDENTIAL), credentialValidator),
								VerifiableCredential.class));
	}

	protected <T> T requireType(Object o, Class<T> clazz) throws ParseException, MalformedURLException {
		if (o == null || !clazz.isInstance(o)) {
			throw new ParseException("Expecting object to be " + clazz.getCanonicalName() + " but it is " + o, 0);
		}
		return clazz.cast(o);
	}

	private <E extends Exception> VerifiableCredential.VerifiableCredentialBuilder<?, ?> unwrapCredential(JSONObject vc,
			BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator) throws ParseException {
		Set<String> types = getTypes(vc);
		Set<String> contexts = getContexts(vc);
		boolean vcType = !types.remove(VERIFIABLE_CREDENTIAL_TYPE);
		boolean vcContext = !contexts.remove(CREDENTIALS_CONTEXT);
		if (vcType && vcContext) {
			throw new ParseException(
					"Expecting VC to contain type " + VERIFIABLE_CREDENTIAL + " and context " + CREDENTIALS_CONTEXT, 0);
		}
		VerifiableCredential.VerifiableCredentialBuilder<?, ?> builder;
		if (types.remove(VALIDATED_QUERY_CREDENTIAL_TYPE)) {
			builder = unwrapValidatedQuery(vc, types, contexts, credentialValidator);
		} else {
			builder = unwrapCredentialExtension(vc, types, contexts, credentialValidator);

		}
		if (!types.isEmpty()) {
			log.info("Received unexpected types; they are ignored " + types);
		}
		if (!contexts.isEmpty()) {
			log.info("Received unexpected contexts; they are ignored " + contexts);
		}
		return builder //
		;
	}

	private <E extends Exception> ValidatedQuery.ValidatedQueryBuilder<?, ?> unwrapValidatedQuery(JSONObject vc,
			Set<String> types, Set<String> contexts,
			BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator) throws ParseException {
		if (!contexts.remove(VALIDATED_QUERY_CONTEXT)) {
			throw new ParseException("Expecting validated query to contain context " + VALIDATED_QUERY_CONTEXT, 0);
		}
		JSONObject subject = getRequiredJSONObject(vc, CREDENTIAL_SUBJECT);
		return ValidatedQuery.builder() //
				.subjectId(getRequiredString(subject, ID)) //
				.query(getRequiredString(subject, QUERY)) //
				.profile(getRequiredString(subject, PROFILE)) //
				.ontology(getRequiredString(subject, ONTOLOGY)) //
		;
	}

	private <E extends Exception> VerifiableCredential.VerifiableCredentialBuilder<?, ?> unwrapCredentialExtension(
			JSONObject vc, Set<String> types, Set<String> contexts,
			BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator) throws ParseException {
		throw new ParseException("Received unexected VC; types = " + types + "; contexts = " + contexts, 0);
	}

	private Set<String> getTypes(JSONObject o) throws ParseException {
		return new HashSet<String>(getList(o, TYPE, String.class));
	}

	private Set<String> getContexts(JSONObject o) throws ParseException {
		return new HashSet<String>(getList(o, CONTEXT, String.class));
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

	public void validateIntegrity(VerifiableBase v) throws ParseException {
		log.trace("Validating integrity of {}", v.getId());
		if (v instanceof VerifiablePresentation) {
			validateIntegrity((VerifiablePresentation) v);
		} else if (v instanceof VerifiableCredential) {
			validateIntegrity((VerifiableCredential) v);
		} else {
			validateIntegrityExtension(v);
		}
	}

	protected void validateIntegrity(VerifiablePresentation v) throws ParseException {
		validateIntegrityExtension(v);
	}

	protected void validateIntegrity(VerifiableCredential vc) throws ParseException {
		if (vc instanceof ValidatedQuery) {
			validateIntegrity((ValidatedQuery) vc);
		} else {
			validateIntegrityExtension(vc);
		}
	}

	protected void validateIntegrity(ValidatedQuery vq) throws ParseException {
		validateIntegrityExtension(vq);
	}

	protected void validateIntegrityExtension(ValidatedQuery v) throws ParseException {
	}

	protected void validateIntegrityExtension(VerifiablePresentation v) throws ParseException {
	}

	protected void validateIntegrityExtension(VerifiableBase v) throws ParseException {
		throw new ParseException("Received unexpected credental type " + v.getClass().getCanonicalName(), 0);
	}

	protected void validateIntegrityExtension(VerifiableCredential v) throws ParseException {
		throw new ParseException("Received unexpected credental type " + v.getClass().getCanonicalName(), 0);
	}

	public void validateFields(VerifiableBase v) throws ParseException {
		log.trace("Validating fields of {}", v.getId());
		if (StringUtils.isBlank(v.getFrom())) {
			throw new ParseException("Required feld `aud' is not given", 0);
		}
		if (StringUtils.isBlank(v.getFrom())) {
			throw new ParseException("Required feld `iss' is not given", 0);
		}
		if (StringUtils.isBlank(v.getId())) {
			throw new ParseException("Required feld `jti' is not given", 0);
		}
		if (StringUtils.isBlank(v.getKeyId())) {
			throw new ParseException("Required feld `kid' is not given", 0);
		}
		if (v.getValidFrom() != null && v.getValidFrom().isAfter(ZonedDateTime.now())) {
			throw new ParseException("VC is not valid yet (from " + v.getValidFrom() + ")", 0);
		}
		if (v.getExpiration() != null && v.getExpiration().isBefore(ZonedDateTime.now())) {
			throw new ParseException("VC is no longer valid (to " + v.getExpiration() + ")", 0);
		}
		if (v instanceof VerifiablePresentation) {
			validateFields((VerifiablePresentation) v);
		} else if (v instanceof VerifiableCredential) {
			validateFields((VerifiableCredential) v);
		} else {
			validateFieldsExtension(v);
		}
	}

	protected void validateFields(VerifiablePresentation vp) throws ParseException {
		if (vp.getCredential() == null) {
			if (vp.getExternalCredential() == null) {
				throw new ParseException("A VC must be given", 0);
			} else {
				throw new ParseException("A VC must be given and parsed", 0);
			}
		}
		validateFieldsExtension(vp);
	}

	protected void validateFields(VerifiableCredential vc) throws ParseException {
		if (vc instanceof ValidatedQuery) {
			validateFields((ValidatedQuery) vc);
		} else {
			validateFieldsExtension(vc);
		}
	}

	protected void validateFields(ValidatedQuery vq) throws ParseException {
		if (StringUtils.isBlank(vq.getQuery())) {
			throw new ParseException("Required feld `query' is not given", 0);
		}
		if (StringUtils.isBlank(vq.getOntology())) {
			throw new ParseException("Required feld `ontology' is not given", 0);
		}
		if (StringUtils.isBlank(vq.getProfile())) {
			throw new ParseException("Required feld `profile' is not given", 0);
		}
		validateFieldsExtension(vq);
	}

	protected void validateFieldsExtension(VerifiableBase v) throws ParseException {
		throw new ParseException("Received unexpected credental type " + v.getClass().getCanonicalName(), 0);
	}

	protected void validateFieldsExtension(VerifiableCredential vc) throws ParseException {
		throw new ParseException("Received unexpected credental type " + vc.getClass().getCanonicalName(), 0);
	}

	protected void validateFieldsExtension(VerifiablePresentation vp) throws ParseException {
	}

	protected void validateFieldsExtension(ValidatedQuery vq) throws ParseException {
	}

	public <T extends VerifiableBase> void validateMessageIntegrity(Message<T> message, T body) throws ParseException {
	}

}
