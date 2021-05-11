package nl.kik.datastation.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.ErrorReport;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.ds.async.Request;
import nl.kik.datastation.dto.ds.async.Response;
import nl.kik.datastation.util.FunctionWrapper.BiConsumerWithException;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

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

	public <T, E extends Exception> JWSObject wrap(Message<T> m, FunctionWithException<T, JSONObject, E> bodyEncoder)
			throws E {
		m = fillDefaults(m);
		JWSHeader header = new JWSHeader(JWSAlgorithm.EdDSA, JWM, null, null, null, null, null, null, null, null,
				m.getKeyId(), true, null, null);

		Collection<String> recipient = CollectionUtils.emptyIfNull(m.getTo());
		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder() //
				.jwtID(m.getId()) //
				.claim(FROM, m.getFrom()) //
				.claim(TO, recipient.size() == 1 ? recipient.iterator().next() : recipient) //
				.notBeforeTime(m.getValidFrom() == null ? null : Date.from(m.getValidFrom().toInstant())) //
				.expirationTime(m.getExpiration() == null ? null : Date.from(m.getExpiration().toInstant())) //
				.issueTime(m.getCreation() == null ? null : Date.from(m.getCreation().toInstant())) //
				.claim(THREAD, m.getThreadId()) //
				.claim(BODY, m.getBody() == null ? null : bodyEncoder.apply(m.getBody())) //
		;
		claims = wrap(claims, m);

		return new SignedJWT(header, claims.build());
	}

	public <T, E extends Exception> FunctionWithException<Message<T>, JOSEObject, E> wrap(
			FunctionWithException<T, JSONObject, E> bodyEncoder) {
		return m -> wrap(m, bodyEncoder);
	}

	public <T, E extends Exception> FunctionWithException<T, JSONObject, E> base64Wrapper(
			FunctionWithException<T, ? extends JOSEObject, E> preprocessor) {
		return o -> new JSONObject(Map.of(MESSAGE, preprocessor.apply(o).serialize()));
	}

	public <T, E extends Exception> FunctionWithException<JSONObject, T, E> base64Unwrapper(
			FunctionWithException<String, T, E> preprocessor) {
		return o -> preprocessor.apply(o.getAsString(MESSAGE));
	}

	public <E extends Exception> FunctionWithException<String, JSONObject, E> rawStringWrapper() {
		return s -> new JSONObject(Map.of(MESSAGE, s));
	}

	public <E extends Exception> FunctionWithException<JSONObject, String, E> rawStringUnwrapper() {
		return o -> o.getAsString(MESSAGE);
	}

	public <T, E extends Exception> Message<T> unwrapMessage(String encoded,
			BiConsumerWithException<JWSObject, Message<T>, E> credentialValidator,
			FunctionWithException<JSONObject, T, E> bodyDecoder) throws ParseException, MalformedURLException, E {
		SignedJWT object = SignedJWT.parse(encoded);
		JWSHeader header = object.getHeader();
		JWTClaimsSet claims = object.getJWTClaimsSet();
		checkEquals("jti", JWM, header.getType());
		checkEquals("alg", JWSAlgorithm.EdDSA, header.getAlgorithm());

		List<String> recipient;
		try {
			recipient = claims.getStringListClaim(TO);
		} catch (ParseException e) {
			recipient = Collections.singletonList(claims.getStringClaim(TO));
		}
		Message<T> result = unwrap(claims, bodyDecoder) //
				.id(claims.getJWTID()) //
				.keyId(header.getKeyID()) //
				.from(claims.getStringClaim(FROM)) //
				.to(recipient) //
				.expiration(claims.getExpirationTime() == null ? null
						: claims.getExpirationTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.creation(claims.getIssueTime() == null ? null
						: claims.getIssueTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.validFrom(claims.getNotBeforeTime() == null ? null
						: claims.getNotBeforeTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.threadId(claims.getStringClaim(THREAD)) //
				.body(bodyDecoder.apply(claims.getJSONObjectClaim(BODY))) //
				.build();
		credentialValidator.accept(object, result);
		return result;
	}

	private <T, E extends Exception> Message.MessageBuilder<T, ?, ?> unwrap(JWTClaimsSet claims,
			FunctionWithException<JSONObject, T, E> bodyDecoder) throws ParseException, MalformedURLException {
		String type = getRequiredString(claims, TYPE);
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

	/**
	 * @param type
	 * @throws ParseException
	 */
	protected <T, E extends Exception> Message.MessageBuilder<T, ?, ?> unwrapExtension(JWTClaimsSet claims,
			FunctionWithException<JSONObject, T, E> bodyDecoder, String type)
			throws ParseException, MalformedURLException {
		throw new ParseException("message has invalid type " + type, 0);
	}

	private <T, E extends Exception> ErrorReport.ErrorReportBuilder<T, ?, ?> unwrapErrorReport(JWTClaimsSet claims,
			FunctionWithException<JSONObject, T, E> bodyDecoder) throws MalformedURLException, ParseException {
		return ErrorReport.<T>builder() //
		;
	}

	private <T, E extends Exception> Response.ResponseBuilder<T, ?, ?> unwrapResponse(JWTClaimsSet claims,
			FunctionWithException<JSONObject, T, E> bodyDecoder) throws MalformedURLException, ParseException {
		return Response.<T>builder() //
		;
	}

	private <T, E extends Exception> Request.RequestBuilder<T, ?, ?> unwrapRequest(JWTClaimsSet claims,
			FunctionWithException<JSONObject, T, E> bodyDecoder) throws MalformedURLException, ParseException {
		return Request.<T>builder() //
				.replyUrl(new URL(getRequiredString(claims, REPLY_URL))) //
		;
	}

	private Builder wrap(Builder claims, Message<?> m) {
		if (m instanceof Request) {
			return wrap(claims, (Request<?>) m);
		}
		if (m instanceof Response<?>) {
			return wrap(claims, (Response<?>) m);
		}
		if (m instanceof ErrorReport<?>) {
			return wrap(claims, (ErrorReport<?>) m);
		}
		return wrapExtension(claims, m);
	}

	/**
	 * @param m
	 */
	protected Builder wrapExtension(Builder claims, Message<?> m) {
		throw new IllegalArgumentException(
				"Received unexpected subclass of Message (" + m.getClass().getCanonicalName() + ")");
	}

	private Builder wrap(Builder claims, Request<?> m) {
		return claims //
				.claim(TYPE, REQUEST) //
				.claim(REPLY_URL, m.getReplyUrl() == null ? null : m.getReplyUrl().toString()) //
		;
	}

	private Builder wrap(Builder claims, Response<?> m) {
		return claims //
				.claim(TYPE, RESPONSE) //
		;
	}

	private Builder wrap(Builder claims, ErrorReport<?> m) {
		return claims //
				.claim(TYPE, ERROR_REPORT) //
		;
	}

	private <T> Message<T> fillDefaults(Message<T> m) {
		return fillDefaultsExtension(m.toBuilder() //
				.id(m.getId() == null ? randomUUID() : m.getId()) //
				.creation(m.getCreation() == null ? OffsetDateTime.now().toZonedDateTime() : m.getCreation()) //
				.threadId(m.getThreadId() == null ? randomUUID() : m.getThreadId()) //
				.build());
	}

	protected <T> Message<T> fillDefaultsExtension(Message<T> m) {
		return m;
	}

	public <T> void validateFields(Message<T> m) throws ParseException {
		log.trace("Validating fields of {}", m.getId());
		if (StringUtils.isBlank(m.getFrom())) {
			throw new ParseException("Required feld `from' is not given", 0);
		}
		if (CollectionUtils.isEmpty(m.getTo())) {
			throw new ParseException("Required feld `to' is not given", 0);
		}
		if (StringUtils.isBlank(m.getId())) {
			throw new ParseException("Required feld `jti' is not given", 0);
		}
		if (StringUtils.isBlank(m.getKeyId())) {
			throw new ParseException("Required feld `kid' is not given", 0);
		}
		if (StringUtils.isBlank(m.getThreadId())) {
			throw new ParseException("Required feld `threadId' is not given", 0);
		}
		if (m.getValidFrom() != null && m.getValidFrom().isAfter(ZonedDateTime.now())) {
			throw new ParseException("Message is not valid yet (from " + m.getValidFrom() + ")", 0);
		}
		if (m.getExpiration() != null && m.getExpiration().isBefore(ZonedDateTime.now())) {
			throw new ParseException("Message is no longer valid (to " + m.getExpiration() + ")", 0);
		}
		if (m.getBody() == null) {
			throw new ParseException("A body must be given", 0);
		}

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

	protected <T> void validateFields(Request<T> m) throws ParseException {
		if (m.getReplyUrl() == null) {
			throw new ParseException("Required feld `replyUrl' is not given", 0);
		}
		validateFieldsExtension(m);
	}

	protected <T> void validateFields(Response<T> m) throws ParseException {
		validateFieldsExtension(m);
	}

	protected <T> void validateFields(ErrorReport<T> m) throws ParseException {
		validateFieldsExtension(m);
	}

	protected <T> void validateFieldsExtension(Request<T> m) throws ParseException {
	}

	protected <T> void validateFieldsExtension(Response<T> m) throws ParseException {
	}

	protected <T> void validateFieldsExtension(ErrorReport<T> m) throws ParseException {
	}

	protected <T> void validateFieldsExtension(Message<T> m) throws ParseException {
		throw new ParseException("Received unexpected message type " + m.getClass().getCanonicalName(), 0);
	}

	public <T, E extends Exception> void validateIntegrity(Message<T> m,
			BiConsumerWithException<Message<T>, T, E> delegate) throws ParseException, E {
		log.trace("Validating integrity of {}", m.getId());
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

	protected <T> void validateIntegrity(Request<T> m) throws ParseException {
		validateIntegrityExtension(m);
	}

	protected <T> void validateIntegrity(Response<T> m) throws ParseException {
		validateIntegrityExtension(m);
	}

	protected <T> void validateIntegrity(ErrorReport<T> m) throws ParseException {
		validateIntegrityExtension(m);
	}

	protected <T> void validateIntegrityExtension(Request<T> m) throws ParseException {
	}

	protected <T> void validateIntegrityExtension(Response<T> m) throws ParseException {
	}

	protected <T> void validateIntegrityExtension(ErrorReport<T> m) throws ParseException {
	}

	protected <T> void validateIntegrityExtension(Message<T> m) throws ParseException {
		throw new ParseException("Received unexpected message type " + m.getClass().getCanonicalName(), 0);
	}

}
