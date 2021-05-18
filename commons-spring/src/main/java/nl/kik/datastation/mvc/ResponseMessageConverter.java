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
import net.minidev.json.JSONObject;
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
	private ResultService resultService;

	public ResponseMessageConverter(MessageService service, ResultService resultService, KeyService keys,
			ValidationService validator) {
		super(service, keys, validator);
		this.resultService = resultService;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<ReturnMessage<Object>> getMessageClass() {
		return (Class) ReturnMessage.class;
	}

	@Override
	protected Class<Object> getBodyClass() {
		return Object.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ReturnMessage<Object> decodeMessage(String s, HttpInputMessage inputMessage) {
		try {
			SignedJWT object = SignedJWT.parse(s);
			JWTClaimsSet claims = object.getJWTClaimsSet();
			String type = StringUtils.trimToEmpty(claims.getStringClaim(MessageService.TYPE));
			switch (type) {
			case MessageService.RESPONSE: {
				Message<Map<String, Result>> message = service
						.unwrapMessage(s, this.validator::validate,
								o -> resultService.<Result, Exception>unwrapResultSet(
										JSONObjectUtils.getJSONObject(o, MessageService.MESSAGE),
										resultService::unwrap));
				if (!Response.class.isInstance(message)) {
					throw new ParseException("Message must be Response and body must be result set of Result", 0);
				}
				return (Response) message;
			}
			case MessageService.ERROR_REPORT: {
				Message<String> message = service.unwrapMessage(s, this.validator::validate,
						o -> o.getAsString(MessageService.MESSAGE));
				if (!ErrorReport.class.isInstance(message)) {
					throw new ParseException("Message must be ErrorReport and body must be String", 0);
				}
				return (ErrorReport) message;
			}
			default:
				throw new ParseException("Message of type " + type + " not supported for return messages", 0);
			}

		} catch (Exception e) {
			log.trace("Exception", e);
			throw new HttpMessageNotReadableException("Unable to parse Message", e, inputMessage);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected JWSObject encodeMessage(ReturnMessage<Object> t, HttpOutputMessage outputMessage)
			throws Exception, JOSEException {
		JWSObject wrapped;
		if (t instanceof ErrorReport<?> && t.getBody() instanceof String) {
			wrapped = service.wrap((ErrorReport<String>) (ErrorReport) t,
					s -> new JSONObject(Map.of(MessageService.MESSAGE, s)));
		} else if (t instanceof Response<?> && t.getBody() instanceof Map
				&& ((Map<?, ?>) t.getBody()).keySet().stream().allMatch(String.class::isInstance)
				&& ((Map<?, ?>) t.getBody()).values().stream().allMatch(Result.class::isInstance)) {
			wrapped = service.wrap((Response<Map<String, Result>>) (Response) t, s -> new JSONObject(
					Map.of(MessageService.MESSAGE, resultService.wrapResultSet(s, resultService::wrap))));
		} else {
			throw new ParseException("Received unsupported return message", 0);
		}
		validator.sign(wrapped, keys.getSigner(wrapped.getHeader().getAlgorithm(), t.getIssuer(), t.getKeyId()));
		return wrapped;
	}

	@Override
	protected FunctionWithException<JSONObject, ?, Exception> getDecoder(HttpInputMessage inputMessage) {
		return null;
	}

	@Override
	protected FunctionWithException<Object, JSONObject, Exception> getEncoder(HttpOutputMessage outputMessage) {
		return null;
	}

}
