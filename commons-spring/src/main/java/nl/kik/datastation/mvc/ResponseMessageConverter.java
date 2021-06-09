package nl.kik.datastation.mvc;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.ds.Result;
import nl.kik.datastation.dto.ds.async.ErrorReport;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.ds.async.Response;
import nl.kik.datastation.dto.ds.async.ReturnMessage;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.MessageService;
import nl.kik.datastation.service.ResultService;
import nl.kik.datastation.service.ValidationService;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

@Slf4j
public class ResponseMessageConverter extends MessageMessageConverter<Object, ReturnMessage<Object>> {
	private final ResultService resultService;

	public ResponseMessageConverter(final MessageService service, final ResultService resultService,
			final KeyService keys, final ValidationService validator) {
		super(service, keys, validator);
		this.resultService = resultService;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ReturnMessage<Object> decodeMessage(final String s, final HttpInputMessage inputMessage) {
		try {
			final SignedJWT object = SignedJWT.parse(s);
			final JWTClaimsSet claims = object.getJWTClaimsSet();
			final String type = StringUtils.trimToEmpty(claims.getStringClaim(MessageService.TYPE));
			switch (type) {
			case MessageService.RESPONSE: {
				final Message<Map<String, Result>> message = service
						.unwrapMessage(s, validator::validate,
								o -> resultService.<Result, Exception>unwrapResultSet(
										JSONObjectUtils.getJSONObject(o, MessageService.MESSAGE),
										resultService::unwrap));
				if (!Response.class.isInstance(message))
					throw new ParseException("Message must be Response and body must be result set of Result", 0);
				return (Response) message;
			}
			case MessageService.ERROR_REPORT: {
				final Message<String> message = service.unwrapMessage(s, validator::validate,
						o -> JSONObjectUtils.getString(o, MessageService.MESSAGE));
				if (!ErrorReport.class.isInstance(message))
					throw new ParseException("Message must be ErrorReport and body must be String", 0);
				return (ErrorReport) message;
			}
			default:
				throw new ParseException("Message of type " + type + " not supported for return messages", 0);
			}

		} catch (final Exception e) {
			ResponseMessageConverter.log.trace("Exception", e);
			throw new HttpMessageNotReadableException("Unable to parse Message", e, inputMessage);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected JWSObject encodeMessage(final ReturnMessage<Object> t, final HttpOutputMessage outputMessage)
			throws Exception, JOSEException {
		JWSObject wrapped;
		if (t instanceof ErrorReport<?> && t.getBody() instanceof String) {
			wrapped = service.wrap((ErrorReport<String>) (ErrorReport) t, s -> Map.of(MessageService.MESSAGE, s));
		} else if (t instanceof Response<?> && t.getBody() instanceof Map
				&& ((Map<?, ?>) t.getBody()).keySet().stream().allMatch(String.class::isInstance)
				&& ((Map<?, ?>) t.getBody()).values().stream().allMatch(Result.class::isInstance)) {
			wrapped = service.wrap((Response<Map<String, Result>>) (Response) t,
					s -> Map.of(MessageService.MESSAGE, resultService.wrapResultSet(s, resultService::wrap)));
		} else
			throw new ParseException("Received unsupported return message", 0);
		validator.sign(wrapped, keys.getSigner(wrapped.getHeader().getAlgorithm(), t.getIssuer(), t.getKeyId()));
		return wrapped;
	}

	@Override
	protected Class<Object> getBodyClass() {
		return Object.class;
	}

	@Override
	protected FunctionWithException<Map<String, Object>, ?, Exception> getDecoder(final HttpInputMessage inputMessage) {
		return null;
	}

	@Override
	protected FunctionWithException<Object, Map<String, Object>, Exception> getEncoder(
			final HttpOutputMessage outputMessage) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<ReturnMessage<Object>> getMessageClass() {
		return (Class) ReturnMessage.class;
	}

}
