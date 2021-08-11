package nl.kik.commons.datastation.service;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.ds.async.Message;
import nl.kik.commons.datastation.dto.vc.ValidatedQuery;
import nl.kik.commons.datastation.dto.vc.ValidatedQuery.ValidatedQueryBuilder;
import nl.kik.commons.datastation.dto.vc.VerifiableBase;
import nl.kik.commons.datastation.dto.vc.VerifiableCredential;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation.VerifiablePresentationBuilder;
import nl.kik.commons.datastation.util.FunctionWrapper;
import nl.kik.commons.datastation.util.FunctionWrapper.BiConsumerWithException;
import nl.kik.commons.datastation.util.FunctionWrapper.BiFunctionWithException;
import nl.kik.commons.datastation.util.FunctionWrapper.FunctionWithException;

@Slf4j
public class VerifiableCredentialService extends AbstractTokenService {
	public static class VCBuilder {
		public static VCBuilder builder() {
			return new VCBuilder();
		}

		private final Map<String, Object> result;
		private final List<String> context;

		private final List<String> type;

		private VCBuilder() {
			result = new HashMap<>();
			context = new ArrayList<>();
			type = new ArrayList<>();
			result.put(VerifiableCredentialService.CONTEXT, context);
			result.put(VerifiableCredentialService.TYPE, type);
		}

		public Map<String, Object> build() {
			return result;
		}

		public VCBuilder claim(final String key, final Object value) {
			result.put(key, value);
			return this;
		}

		public VCBuilder context(final List<String> cs) {
			context.removeAll(cs);
			context.addAll(cs);
			return this;
		}

		public VCBuilder context(final String... cs) {
			return context(Arrays.asList(cs));
		}

		public VCBuilder type(final List<String> ts) {
			type.removeAll(ts);
			type.addAll(ts);
			return this;
		}

		public VCBuilder type(final String... ts) {
			return type(Arrays.asList(ts));
		}
	}

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

	protected static final String VALIDATED_QUERY_CREDENTIAL_TYPE = "ValidQueryCredential";

	@SuppressWarnings("unchecked")
	private <T extends VerifiableBase> T fillDefaults(final T m) {
		return (T) m.toBuilder() //
				.id(m.getId() == null ? randomUUID() : m.getId()) //
				.creation(m.getCreation() == null ? OffsetDateTime.now().toZonedDateTime() : m.getCreation()) //
				.validFrom(m.getValidFrom() == null ? OffsetDateTime.now().toZonedDateTime() : m.getValidFrom()) //
				.build();
	}

	private Set<String> getContexts(final Map<String, Object> o) throws ParseException {
		return new HashSet<>(getList(o, VerifiableCredentialService.CONTEXT, String.class));
	}

	private Set<String> getTypes(final Map<String, Object> o) throws ParseException {
		return new HashSet<>(getList(o, VerifiableCredentialService.TYPE, String.class));
	}

	private Map<String, Object> makeCredential(final VerifiableCredential m) {
		VCBuilder builder = VCBuilder.builder() //
				.context(VerifiableCredentialService.CREDENTIALS_CONTEXT) //
				.type(VerifiableCredentialService.VERIFIABLE_CREDENTIAL_TYPE) //
		;
		if (m instanceof ValidatedQuery) {
			builder = makeCrendential(builder, (ValidatedQuery) m);
		} else {
			builder = makeCredentialExtension(builder, m);
		}
		return builder.build();
	}

	protected VCBuilder makeCredentialExtension(final VCBuilder builder, final ValidatedQuery m) {
		return builder;
	}

	protected VCBuilder makeCredentialExtension(final VCBuilder builder, final VerifiableCredential m) {
		return builder;
	}

	private VCBuilder makeCrendential(VCBuilder builder, final ValidatedQuery m) {
		builder = builder //
				.context(VerifiableCredentialService.VALIDATED_QUERY_CONTEXT) //
				.type(VerifiableCredentialService.VALIDATED_QUERY_CREDENTIAL_TYPE) //
				.claim(VerifiableCredentialService.CREDENTIAL_SUBJECT, Map.of(//
						VerifiableCredentialService.PROFILE, m.getProfile(), //
						VerifiableCredentialService.ONTOLOGY, m.getOntology(), //
						VerifiableCredentialService.ID, m.getSubjectId() == null ? randomUUID() : m.getSubjectId(), //
						VerifiableCredentialService.QUERY, m.getQuery() //
				)); //
		builder = makeCredentialExtension(builder, m);
		return builder;
	}

	/**
	 * @return
	 */
	protected VerifiablePresentationBuilder<?, ?> makePresentation() {
		return VerifiablePresentation.builder();
	}

	@SuppressWarnings("unchecked")
	private <E extends Exception> Map<String, Object> makePresentation(final VerifiablePresentation m,
			final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		List<JWSObject> credential = m.getExternalCredential();
		if (CollectionUtils.isEmpty(credential)) {
			try {
				credential = CollectionUtils.emptyIfNull(m.getCredential()).stream() //
						.map(FunctionWrapper
								.wrapper((final VerifiableCredential vc) -> credentialSigner.apply(vc, wrap(vc, credentialSigner)))) //
						.collect(Collectors.toList());
			} catch (final RuntimeException e) {
				throw (E) e.getCause();
			}
		}
		VCBuilder builder = VCBuilder.builder() //
				.context(VerifiableCredentialService.CREDENTIALS_CONTEXT) //
				.type(VerifiableCredentialService.VERIFIABLE_PRESENTATION_TYPE) //
				.claim(VerifiableCredentialService.VERIFIABLE_CREDENTIAL,
						credential.stream().map(JWSObject::serialize).collect(Collectors.toList()))//
		;
		builder = makePresentationExtension(builder, m, credentialSigner);
		return builder.build();
	}

	protected <E extends Exception> VCBuilder makePresentationExtension(final VCBuilder builder,
			final VerifiablePresentation m,
			final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		return builder;
	}

	/**
	 * @return
	 */
	protected ValidatedQueryBuilder<?, ?> makeValidatedQuery() {
		return ValidatedQuery.builder();
	}

	protected <T> T requireType(final Object o, final Class<T> clazz) throws ParseException, MalformedURLException {
		if (o == null || !clazz.isInstance(o))
			throw new ParseException("Expecting object to be " + clazz.getCanonicalName() + " but it is " + o, 0);
		return clazz.cast(o);
	}

	private <E extends Exception> VerifiableBase.VerifiableBaseBuilder<?, ?> unwrap(final JWTClaimsSet claims,
			final BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator)
			throws ParseException, MalformedURLException, E {
		final Map<String, Object> vp = claims.getJSONObjectClaim(VerifiableCredentialService.VP);
		final Map<String, Object> vc = claims.getJSONObjectClaim(VerifiableCredentialService.VC);
		if (!(vp == null ^ vc == null))
			throw new ParseException("Exactly one of vc or vp must be given", 0);
		if (vp != null)
			return unwrapPresentation(vp, credentialValidator);
		if (vc != null)
			return unwrapCredential(vc, credentialValidator);
		throw new IllegalArgumentException("It should not be possible to reach this code");
	}

	private <E extends Exception> VerifiableCredential.VerifiableCredentialBuilder<?, ?> unwrapCredential(
			final Map<String, Object> vc, final BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator)
			throws ParseException {
		final Set<String> types = getTypes(vc);
		final Set<String> contexts = getContexts(vc);
		final boolean vcType = !types.remove(VerifiableCredentialService.VERIFIABLE_CREDENTIAL_TYPE);
		final boolean vcContext = !contexts.remove(VerifiableCredentialService.CREDENTIALS_CONTEXT);
		if (vcType && vcContext)
			throw new ParseException("Expecting VC to contain type " + VerifiableCredentialService.VERIFIABLE_CREDENTIAL
					+ " and context " + VerifiableCredentialService.CREDENTIALS_CONTEXT, 0);
		VerifiableCredential.VerifiableCredentialBuilder<?, ?> builder;
		if (types.remove(VerifiableCredentialService.VALIDATED_QUERY_CREDENTIAL_TYPE)) {
			builder = unwrapValidatedQuery(vc, types, contexts, credentialValidator);
		} else {
			builder = unwrapCredentialExtension(vc, types, contexts, credentialValidator);

		}
		if (!types.isEmpty()) {
			VerifiableCredentialService.log.info("Received unexpected types; they are ignored " + types);
		}
		if (!contexts.isEmpty()) {
			VerifiableCredentialService.log.info("Received unexpected contexts; they are ignored " + contexts);
		}
		return builder //
		;
	}

	private <E extends Exception> VerifiableCredential.VerifiableCredentialBuilder<?, ?> unwrapCredentialExtension(
			final Map<String, Object> vc, final Set<String> types, final Set<String> contexts,
			final BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator) throws ParseException {
		throw new ParseException("Received unexected VC; types = " + types + "; contexts = " + contexts, 0);
	}

	@SuppressWarnings("unchecked")
	private <E extends Exception> VerifiablePresentation.VerifiablePresentationBuilder<?, ?> unwrapPresentation(
			final Map<String, Object> vp, final BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator)
			throws ParseException, MalformedURLException, E {
		final Set<String> types = getTypes(vp);
		final Set<String> contexts = getContexts(vp);
		final boolean vpType = !types.remove(VerifiableCredentialService.VERIFIABLE_PRESENTATION_TYPE);
		final boolean vpContext = !contexts.remove(VerifiableCredentialService.CREDENTIALS_CONTEXT);
		if (vpType && vpContext)
			throw new ParseException(
					"Expecting VC to contain type " + VerifiableCredentialService.VERIFIABLE_PRESENTATION_TYPE + " and context "
							+ VerifiableCredentialService.CREDENTIALS_CONTEXT,
					0);
		try {
			return makePresentation() //
					.credential(CollectionUtils
							.emptyIfNull(JSONObjectUtils.getStringList(vp, VerifiableCredentialService.VERIFIABLE_CREDENTIAL))
							.stream() //
							.map(FunctionWrapper.wrapper((final String s) -> unwrapVerifiable(s, credentialValidator))) //
							.map(FunctionWrapper.wrapper((final VerifiableBase vc) -> requireType(vc, VerifiableCredential.class))) //
							.collect(Collectors.toList())) //
			;
		} catch (final RuntimeException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof ParseException)
				throw (ParseException) cause;
			if (cause instanceof MalformedURLException)
				throw (MalformedURLException) cause;
			throw (E) cause;
		}
	}

	private <E extends Exception> ValidatedQuery.ValidatedQueryBuilder<?, ?> unwrapValidatedQuery(
			final Map<String, Object> vc, final Set<String> types, final Set<String> contexts,
			final BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator) throws ParseException {
		if (!contexts.remove(VerifiableCredentialService.VALIDATED_QUERY_CONTEXT))
			throw new ParseException(
					"Expecting validated query to contain context " + VerifiableCredentialService.VALIDATED_QUERY_CONTEXT, 0);
		final Map<String, Object> subject = getRequiredJSONObject(vc, VerifiableCredentialService.CREDENTIAL_SUBJECT);
		return makeValidatedQuery() //
				.subjectId(getRequiredString(subject, VerifiableCredentialService.ID)) //
				.query(getRequiredString(subject, VerifiableCredentialService.QUERY)) //
				.profile(getRequiredString(subject, VerifiableCredentialService.PROFILE)) //
				.ontology(getRequiredString(subject, VerifiableCredentialService.ONTOLOGY)) //
		;
	}

	public <E extends Exception> VerifiableBase unwrapVerifiable(final String encoded,
			final BiConsumerWithException<JWSObject, VerifiableBase, E> credentialValidator)
			throws E, ParseException, MalformedURLException {
		final SignedJWT object = SignedJWT.parse(encoded);
		final JWSHeader header = object.getHeader();
		final JWTClaimsSet claims = object.getJWTClaimsSet();
		checkEquals("jti", JOSEObjectType.JWT, header.getType());
		checkEquals("alg", JWSAlgorithm.EdDSA, header.getAlgorithm());

		final VerifiableBase result = unwrap(claims, credentialValidator) //
				.id(claims.getJWTID()) //
				.keyId(header.getKeyID()) //
				.issuer(claims.getIssuer()) //
				.audience(CollectionUtils.isEmpty(claims.getAudience()) ? null : claims.getAudience()) //
				.expiration(claims.getExpirationTime() == null ? null
						: claims.getExpirationTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.creation(claims.getIssueTime() == null ? null
						: claims.getIssueTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(claims.getNotBeforeTime() == null ? null
						: claims.getNotBeforeTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toZonedDateTime()) //
				.build();
		credentialValidator.accept(object, result);
		return result;
	}

	protected void validateFields(final ValidatedQuery vq) throws ParseException {
		if (StringUtils.isBlank(vq.getQuery()))
			throw new ParseException("Required feld `query' is not given", 0);
		if (StringUtils.isBlank(vq.getOntology()))
			throw new ParseException("Required feld `ontology' is not given", 0);
		if (StringUtils.isBlank(vq.getProfile()))
			throw new ParseException("Required feld `profile' is not given", 0);
		validateFieldsExtension(vq);
	}

	public void validateFields(final VerifiableBase v) throws ParseException {
		VerifiableCredentialService.log.trace("Validating fields of {}", v.getId());
		if (StringUtils.isBlank(v.getIssuer()))
			throw new ParseException("Required feld `iss' is not given", 0);
		if (StringUtils.isBlank(v.getId()))
			throw new ParseException("Required feld `jti' is not given", 0);
		if (StringUtils.isBlank(v.getKeyId()))
			throw new ParseException("Required feld `kid' is not given", 0);
		if (v.getValidFrom() != null && v.getValidFrom().isAfter(ZonedDateTime.now()))
			throw new ParseException("VC is not valid yet (from " + v.getValidFrom() + ")", 0);
		if (v.getExpiration() != null && v.getExpiration().isBefore(ZonedDateTime.now()))
			throw new ParseException("VC is no longer valid (to " + v.getExpiration() + ")", 0);
		if (v instanceof VerifiablePresentation) {
			validateFields((VerifiablePresentation) v);
		} else if (v instanceof VerifiableCredential) {
			validateFields((VerifiableCredential) v);
		} else {
			validateFieldsExtension(v);
		}
	}

	protected void validateFields(final VerifiableCredential vc) throws ParseException {
		if (vc instanceof ValidatedQuery) {
			validateFields((ValidatedQuery) vc);
		} else {
			validateFieldsExtension(vc);
		}
	}

	protected void validateFields(final VerifiablePresentation vp) throws ParseException {
		if (vp.getCredential() == null) {
			if (vp.getExternalCredential() == null)
				throw new ParseException("A VC must be given", 0);
			throw new ParseException("A VC must be given and parsed", 0);
		}
		validateFieldsExtension(vp);
	}

	protected void validateFieldsExtension(final ValidatedQuery vq) throws ParseException {
	}

	protected void validateFieldsExtension(final VerifiableBase v) throws ParseException {
		throw new ParseException("Received unexpected credental type " + v.getClass().getCanonicalName(), 0);
	}

	protected void validateFieldsExtension(final VerifiableCredential vc) throws ParseException {
		throw new ParseException("Received unexpected credental type " + vc.getClass().getCanonicalName(), 0);
	}

	protected void validateFieldsExtension(final VerifiablePresentation vp) throws ParseException {
	}

	protected void validateIntegrity(final ValidatedQuery vq) throws ParseException {
		validateIntegrityExtension(vq);
	}

	public void validateIntegrity(final VerifiableBase v) throws ParseException {
		VerifiableCredentialService.log.trace("Validating integrity of {}", v.getId());
		if (v instanceof VerifiablePresentation) {
			validateIntegrity((VerifiablePresentation) v);
		} else if (v instanceof VerifiableCredential) {
			validateIntegrity((VerifiableCredential) v);
		} else {
			validateIntegrityExtension(v);
		}
	}

	protected void validateIntegrity(final VerifiableCredential vc) throws ParseException {
		if (vc instanceof ValidatedQuery) {
			validateIntegrity((ValidatedQuery) vc);
		} else {
			validateIntegrityExtension(vc);
		}
	}

	protected void validateIntegrity(final VerifiablePresentation v) throws ParseException {
		validateIntegrityExtension(v);
	}

	protected void validateIntegrityExtension(final ValidatedQuery v) throws ParseException {
	}

	protected void validateIntegrityExtension(final VerifiableBase v) throws ParseException {
		throw new ParseException("Received unexpected credental type " + v.getClass().getCanonicalName(), 0);
	}

	protected void validateIntegrityExtension(final VerifiableCredential v) throws ParseException {
		throw new ParseException("Received unexpected credental type " + v.getClass().getCanonicalName(), 0);
	}

	protected void validateIntegrityExtension(final VerifiablePresentation v) throws ParseException {
	}

	public <T extends VerifiableBase> void validateMessageIntegrity(final Message<T> message, final T body)
			throws ParseException {
	}

	private <E extends Exception> Builder wrap(final Builder claims, final VerifiableBase m,
			final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		if (m instanceof VerifiablePresentation)
			return wrap(claims, (VerifiablePresentation) m, credentialSigner);
		if (m instanceof VerifiableCredential)
			return wrap(claims, (VerifiableCredential) m);
		return wrapExtension(claims, m, credentialSigner);
	}

	private Builder wrap(final Builder claims, final VerifiableCredential m) {
		return claims //
				.claim(VerifiableCredentialService.VC, makeCredential(m)) //
		;
	}

	private <E extends Exception> Builder wrap(final Builder claims, final VerifiablePresentation m,
			final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		return claims //
				.claim(VerifiableCredentialService.VP, makePresentation(m, credentialSigner)) //
		;
	}

	public <E extends Exception> JWSObject wrap(VerifiableBase m,
			final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		m = fillDefaults(m);
		final JWSHeader header = new JWSHeader(JWSAlgorithm.EdDSA, JOSEObjectType.JWT, null, null, null, null, null, null,
				null, null, m.getKeyId(), true, null, null);

		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder() //
				.jwtID(m.getId()) //
				.issuer(m.getIssuer()) //
				.audience(m.getAudience()) //
				.notBeforeTime(m.getValidFrom() == null ? null : Date.from(m.getValidFrom().toInstant())) //
				.expirationTime(m.getExpiration() == null ? null : Date.from(m.getExpiration().toInstant())) //
				.issueTime(m.getCreation() == null ? null : Date.from(m.getCreation().toInstant())) //
		;
		claims = wrap(claims, m, credentialSigner);

		return new SignedJWT(header, claims.build());
	}

	public <T extends VerifiableBase, E extends Exception, F extends Exception, G extends Exception> FunctionWithException<T, JWSObject, Exception> wrapAndSign(
			final BiFunctionWithException<JWSObject, JWSSigner, JWSObject, G> signer,
			final FunctionWithException<T, JWSSigner, F> key,
			final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) {
		return vc -> signer.apply(wrap(vc, credentialSigner), key.apply(vc));
	}

	protected <E extends Exception> Builder wrapExtension(final Builder claims, final VerifiableBase m,
			final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, E> credentialSigner) throws E {
		throw new IllegalArgumentException(
				"Received unexpected subclass of VerifiableBase (" + m.getClass().getCanonicalName() + ")");
	}

}
