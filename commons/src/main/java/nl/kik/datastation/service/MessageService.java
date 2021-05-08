package nl.kik.datastation.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jose.PlainObject;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;

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

	private static final String FROM = "from";
	private static final String TO = "to";
	private static final String THREAD = "thread_id";
	private static final String BODY = "body";
	private static final String TYPE = "type";
	private static final String REPLY_URL = "reply_url";
	private static final String MESSAGE = "message";

	private static final String REQUEST = "v1/basic-message/request";
	private static final String RESPONSE = "v1/basic-message/response";
	private static final String ERROR_REPORT = "v1/error-report";

	public <T, E extends Exception> JOSEObject wrap(Message<T> m, FunctionWithException<T, JSONObject, E> bodyEncoder)
			throws E {
		m = fillDefaults(m);
		PlainHeader header = new PlainHeader(JWM, null, null, null, null);

		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder() //
				.jwtID(m.getId()) //
				.claim(FROM, m.getFrom()) //
				.claim(TO, m.getTo()) //
				.expirationTime(m.getExpiration() == null ? null : Date.from(m.getExpiration().toInstant())) //
				.issueTime(m.getCreation() == null ? null : Date.from(m.getCreation().toInstant())) //
				.claim(THREAD, m.getThreadId()) //
				.claim(BODY, m.getBody() == null ? null : bodyEncoder.apply(m.getBody())) //
		;
		claims = wrap(claims, m);
		Payload payload = new Payload(claims.build().toJSONObject());

		return new PlainObject(header, payload);
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

	public <T, E extends Exception> Message<T> unwrapMessage(String encoded,
			FunctionWithException<JSONObject, T, E> bodyDecoder) throws ParseException, MalformedURLException, E {
		JSONObject json = JSONObjectUtils.parse(encoded);
		PlainHeader header = PlainHeader.parse(getRequiredJSONObject(json, PROTECTED));
		JWTClaimsSet claims = JWTClaimsSet.parse(getRequiredJSONObject(json, PAYLOAD));
//		Payload payload = new Payload(claims.toJSONObject());
//		PlainObject object = new PlainObject(header, payload);
		checkEquals("jti", JWM, header.getType());

		return unwrap(claims, bodyDecoder) //
				.id(claims.getJWTID()) //
				.from(claims.getStringClaim(FROM)) //
				.to(claims.getStringClaim(TO)) //
				.expiration(claims.getExpirationTime() == null ? null
						: claims.getExpirationTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.creation(claims.getIssueTime() == null ? null
						: claims.getIssueTime().toInstant().atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
								.toZonedDateTime()) //
				.threadId(claims.getStringClaim(THREAD)) //
				.body(bodyDecoder.apply(claims.getJSONObjectClaim(BODY))) //
				.build();
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
		log.info("Validating fields of {}", m.getId());
		if (StringUtils.isBlank(m.getFrom())) {
			throw new ParseException("Required feld `from' is not given", 0);
		}
		if (StringUtils.isBlank(m.getTo())) {
			throw new ParseException("Required feld `to' is not given", 0);
		}
		if (StringUtils.isBlank(m.getId())) {
			throw new ParseException("Required feld `jti' is not given", 0);
		}
		if (StringUtils.isBlank(m.getThreadId())) {
			throw new ParseException("Required feld `threadId' is not given", 0);
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
		log.info("Validating integrity of {}", m.getId());
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
