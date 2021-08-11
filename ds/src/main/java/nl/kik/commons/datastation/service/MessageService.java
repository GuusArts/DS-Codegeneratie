package nl.kik.commons.datastation.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.ds.async.ErrorReport;
import nl.kik.commons.datastation.dto.ds.async.ErrorReport.ErrorReportBuilder;
import nl.kik.commons.datastation.dto.ds.async.Message;
import nl.kik.commons.datastation.dto.ds.async.Request;
import nl.kik.commons.datastation.dto.ds.async.Request.RequestBuilder;
import nl.kik.commons.datastation.dto.ds.async.Response;
import nl.kik.commons.datastation.dto.ds.async.Response.ResponseBuilder;
import nl.kik.commons.datastation.util.FunctionWrapper.BiConsumerWithException;
import nl.kik.commons.datastation.util.FunctionWrapper.FunctionWithException;

@Slf4j
public class MessageService extends AbstractTokenService {
	public static final JOSEObjectType JWM = new JOSEObjectType("JWM");

	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String THREAD = "thread_id";
	public static final String BODY = "body";
	public static final String TYPE = "type";
	public static final String REPLY_URL = "reply_url";
	public static final String MESSAGE = "message";

	public static final String REQUEST = "v1/basic-message/request";
	public static final String RESPONSE = "v1/basic-message/response";
	public static final String ERROR_REPORT = "v1/error-report";

	public <T, E extends Exception> FunctionWithException<Map<String, Object>, T, Exception> base64Unwrapper(
			final FunctionWithException<String, T, E> preprocessor) {
		return o -> preprocessor.apply(JSONObjectUtils.getString(o, MessageService.MESSAGE));
	}

	public <T, E extends Exception> FunctionWithException<T, Map<String, Object>, E> base64Wrapper(
			final FunctionWithException<T, ? extends JOSEObject, E> preprocessor) {
		return o -> Map.of(MessageService.MESSAGE, preprocessor.apply(o).serialize());
	}

	private <T> Message<T> fillDefaults(final Message<T> m) {
		return fillDefaultsExtension(m.toBuilder() //
				.id(m.getId() == null ? randomUUID() : m.getId()) //
				.creation(m.getCreation() == null ? OffsetDateTime.now().toZonedDateTime() : m.getCreation()) //
				.threadId(m.getThreadId() == null ? randomUUID() : m.getThreadId()) //
				.build());
	}

	protected <T> Message<T> fillDefaultsExtension(final Message<T> m) {
		return m;
	}

	/**
	 * @return
	 */
	protected <T> ErrorReportBuilder<T, ?, ?> makeErrorReport() {
		return ErrorReport.<T>builder();
	}

	/**
	 * @return
	 */
	protected <T> RequestBuilder<T, ?, ?> makeRequest() {
		return Request.<T>builder();
	}

	/**
	 * @return
	 */
	protected <T> ResponseBuilder<T, ?, ?> makeResponse() {
		return Response.<T>builder();
	}

	public FunctionWithException<Map<String, Object>, String, ParseException> rawStringUnwrapper() {
		return o -> JSONObjectUtils.getString(o, MessageService.MESSAGE);
	}

	public <E extends Exception> FunctionWithException<String, Map<String, Object>, E> rawStringWrapper() {
		return s -> Map.of(MessageService.MESSAGE, s);
	}

	private <T, E extends Exception> Message.MessageBuilder<T, ?, ?> unwrap(final JWTClaimsSet claims,
			final FunctionWithException<Map<String, Object>, T, E> bodyDecoder) throws ParseException, MalformedURLException {
		final String type = getRequiredString(claims, MessageService.TYPE);
		switch (type) {
		case REQUEST:
			return unwrapRequest(claims, bodyDecoder);
		case RESPONSE:
			return unwrapResponse(claims, bodyDecoder);
		case ERROR_REPORT:
			return unwrapErrorReport(claims, bodyDecoder);
		default:
			return unwrapExtension(claims, bodyDecoder, type);
		}
	}

	private <T, E extends Exception> ErrorReport.ErrorReportBuilder<T, ?, ?> unwrapErrorReport(final JWTClaimsSet claims,
			final FunctionWithException<Map<String, Object>, T, E> bodyDecoder) throws MalformedURLException, ParseException {
		return makeErrorReport() //
		;
	}

	/**
	 * @param type
	 * @throws ParseException
	 */
	protected <T, E extends Exception> Message.MessageBuilder<T, ?, ?> unwrapExtension(final JWTClaimsSet claims,
			final FunctionWithException<Map<String, Object>, T, E> bodyDecoder, final String type)
			throws ParseException, MalformedURLException {
		throw new ParseException("message has invalid type " + type, 0);
	}

	public <T, E extends Exception> Message<T> unwrapMessage(final String encoded,
			final BiConsumerWithException<JWSObject, Message<T>, E> credentialValidator,
			final FunctionWithException<Map<String, Object>, T, E> bodyDecoder)
			throws ParseException, MalformedURLException, E {
		final SignedJWT object = SignedJWT.parse(encoded);
		final JWSHeader header = object.getHeader();
		final JWTClaimsSet claims = object.getJWTClaimsSet();
		checkEquals("jti", MessageService.JWM, header.getType());
		checkEquals("alg", JWSAlgorithm.EdDSA, header.getAlgorithm());

		List<String> recipient;
		try {
			recipient = claims.getStringListClaim(MessageService.TO);
		} catch (final ParseException e) {
			recipient = Collections.singletonList(claims.getStringClaim(MessageService.TO));
		}
		final Message<T> result = unwrap(claims, bodyDecoder) //
				.id(claims.getJWTID()) //
				.keyId(header.getKeyID()) //
				.from(claims.getStringClaim(MessageService.FROM)) //
				.to(CollectionUtils.isEmpty(recipient) ? null : recipient) //
				.issuer(claims.getIssuer()) //
				.audience(CollectionUtils.isEmpty(claims.getAudience()) ? null : claims.getAudience()) //
				.expiration(claims.getExpirationTime() == null ? null
						: claims.getExpirationTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.creation(claims.getIssueTime() == null ? null
						: claims.getIssueTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(claims.getNotBeforeTime() == null ? null
						: claims.getNotBeforeTime().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime().toZonedDateTime()) //
				.threadId(claims.getStringClaim(MessageService.THREAD)) //
				.body(bodyDecoder.apply(claims.getJSONObjectClaim(MessageService.BODY))) //
				.build();
		credentialValidator.accept(object, result);
		return result;
	}

	private <T, E extends Exception> Request.RequestBuilder<T, ?, ?> unwrapRequest(final JWTClaimsSet claims,
			final FunctionWithException<Map<String, Object>, T, E> bodyDecoder) throws MalformedURLException, ParseException {
		return this.<T>makeRequest() //
				.replyUrl(new URL(getRequiredString(claims, MessageService.REPLY_URL))) //
		;
	}

	private <T, E extends Exception> Response.ResponseBuilder<T, ?, ?> unwrapResponse(final JWTClaimsSet claims,
			final FunctionWithException<Map<String, Object>, T, E> bodyDecoder) throws MalformedURLException, ParseException {
		return makeResponse() //
		;
	}

	protected <T> void validateFields(final ErrorReport<T> m) throws ParseException {
		validateFieldsExtension(m);
	}

	public <T> void validateFields(final Message<T> m) throws ParseException {
		MessageService.log.trace("Validating fields of {}", m.getId());
		if (StringUtils.isBlank(m.getIssuer()))
			throw new ParseException("Required feld `iss' is not given", 0);
		if (StringUtils.isBlank(m.getFrom()))
			throw new ParseException("Required feld `from' is not given", 0);
		if (CollectionUtils.isEmpty(m.getTo()))
			throw new ParseException("Required feld `to' is not given", 0);
		if (StringUtils.isBlank(m.getId()))
			throw new ParseException("Required feld `jti' is not given", 0);
		if (StringUtils.isBlank(m.getKeyId()))
			throw new ParseException("Required feld `kid' is not given", 0);
		if (StringUtils.isBlank(m.getThreadId()))
			throw new ParseException("Required feld `threadId' is not given", 0);
		if (m.getValidFrom() != null && m.getValidFrom().isAfter(ZonedDateTime.now()))
			throw new ParseException("Message is not valid yet (from " + m.getValidFrom() + ")", 0);
		if (m.getExpiration() != null && m.getExpiration().isBefore(ZonedDateTime.now()))
			throw new ParseException("Message is no longer valid (to " + m.getExpiration() + ")", 0);
		if (m.getBody() == null)
			throw new ParseException("A body must be given", 0);

		if (m instanceof Request<?>) {
			validateFields((Request<T>) m);
		} else if (m instanceof Response<?>) {
			validateFields((Response<T>) m);
		} else if (m instanceof ErrorReport<?>) {
			validateFields((ErrorReport<T>) m);
		} else {
			validateFieldsExtension(m);
		}
	}

	protected <T> void validateFields(final Request<T> m) throws ParseException {
		if (m.getReplyUrl() == null)
			throw new ParseException("Required feld `replyUrl' is not given", 0);
		validateFieldsExtension(m);
	}

	protected <T> void validateFields(final Response<T> m) throws ParseException {
		validateFieldsExtension(m);
	}

	protected <T> void validateFieldsExtension(final ErrorReport<T> m) throws ParseException {
	}

	protected <T> void validateFieldsExtension(final Message<T> m) throws ParseException {
		throw new ParseException("Received unexpected message type " + m.getClass().getCanonicalName(), 0);
	}

	protected <T> void validateFieldsExtension(final Request<T> m) throws ParseException {
	}

	protected <T> void validateFieldsExtension(final Response<T> m) throws ParseException {
	}

	protected <T> void validateIntegrity(final ErrorReport<T> m) throws ParseException {
		validateIntegrityExtension(m);
	}

	public <T, E extends Exception> void validateIntegrity(final Message<T> m,
			final BiConsumerWithException<Message<T>, T, E> delegate) throws ParseException, E {
		MessageService.log.trace("Validating integrity of {}", m.getId());
		delegate.accept(m, m.getBody());
		if (m instanceof Request<?>) {
			validateIntegrity((Request<T>) m);
		} else if (m instanceof Response<?>) {
			validateIntegrity((Response<T>) m);
		} else if (m instanceof ErrorReport<?>) {
			validateIntegrity((ErrorReport<T>) m);
		} else {
			validateIntegrityExtension(m);
		}
	}

	protected <T> void validateIntegrity(final Request<T> m) throws ParseException {
		validateIntegrityExtension(m);
	}

	protected <T> void validateIntegrity(final Response<T> m) throws ParseException {
		validateIntegrityExtension(m);
	}

	protected <T> void validateIntegrityExtension(final ErrorReport<T> m) throws ParseException {
	}

	protected <T> void validateIntegrityExtension(final Message<T> m) throws ParseException {
		throw new ParseException("Received unexpected message type " + m.getClass().getCanonicalName(), 0);
	}

	protected <T> void validateIntegrityExtension(final Request<T> m) throws ParseException {
	}

	protected <T> void validateIntegrityExtension(final Response<T> m) throws ParseException {
	}

	private Builder wrap(final Builder claims, final ErrorReport<?> m) {
		return claims //
				.claim(MessageService.TYPE, MessageService.ERROR_REPORT) //
		;
	}

	private Builder wrap(final Builder claims, final Message<?> m) {
		if (m instanceof Request)
			return wrap(claims, (Request<?>) m);
		if (m instanceof Response<?>)
			return wrap(claims, (Response<?>) m);
		if (m instanceof ErrorReport<?>)
			return wrap(claims, (ErrorReport<?>) m);
		return wrapExtension(claims, m);
	}

	private Builder wrap(final Builder claims, final Request<?> m) {
		return claims //
				.claim(MessageService.TYPE, MessageService.REQUEST) //
				.claim(MessageService.REPLY_URL, m.getReplyUrl() == null ? null : m.getReplyUrl().toString()) //
		;
	}

	private Builder wrap(final Builder claims, final Response<?> m) {
		return claims //
				.claim(MessageService.TYPE, MessageService.RESPONSE) //
		;
	}

	public <T, E extends Exception> FunctionWithException<Message<T>, JOSEObject, E> wrap(
			final FunctionWithException<T, Map<String, Object>, E> bodyEncoder) {
		return m -> wrap(m, bodyEncoder);
	}

	public <T, E extends Exception> JWSObject wrap(Message<T> m,
			final FunctionWithException<T, Map<String, Object>, E> bodyEncoder) throws E {
		m = fillDefaults(m);
		final JWSHeader header = new JWSHeader(JWSAlgorithm.EdDSA, MessageService.JWM, null, null, null, null, null, null,
				null, null, m.getKeyId(), true, null, null);

		final Collection<String> recipient = CollectionUtils.emptyIfNull(m.getTo());
		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder() //
				.jwtID(m.getId()) //
				.issuer(m.getIssuer()) //
				.audience(m.getAudience()) //
				.claim(MessageService.FROM, m.getFrom()) //
				.claim(MessageService.TO, recipient.size() == 1 ? recipient.iterator().next() : recipient) //
				.notBeforeTime(m.getValidFrom() == null ? null : Date.from(m.getValidFrom().toInstant())) //
				.expirationTime(m.getExpiration() == null ? null : Date.from(m.getExpiration().toInstant())) //
				.issueTime(m.getCreation() == null ? null : Date.from(m.getCreation().toInstant())) //
				.claim(MessageService.THREAD, m.getThreadId()) //
				.claim(MessageService.BODY, m.getBody() == null ? null : bodyEncoder.apply(m.getBody())) //
		;
		claims = wrap(claims, m);

		return new SignedJWT(header, claims.build());
	}

	/**
	 * @param m
	 */
	protected Builder wrapExtension(final Builder claims, final Message<?> m) {
		throw new IllegalArgumentException(
				"Received unexpected subclass of Message (" + m.getClass().getCanonicalName() + ")");
	}

}
