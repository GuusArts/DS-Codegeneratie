package nl.kik.datastation.mvc;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.MessageService;
import nl.kik.datastation.service.ValidationService;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

@Slf4j
public abstract class MessageMessageConverter<T, M extends Message<T>> extends AbstractHttpMessageConverter<M> {
	protected static final Charset UTF8 = Charset.forName("UTF-8");
	protected MessageService service;
	protected KeyService keys;
	protected ValidationService validator;

	public MessageMessageConverter(MessageService service, KeyService keys, ValidationService validator) {
		super(MediaType.ALL);
		this.service = service;
		this.keys = keys;
		this.validator = validator;
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return getMessageClass().isAssignableFrom(clazz);
	}

	protected abstract Class<M> getMessageClass();

	protected abstract Class<T> getBodyClass();

	public M decode(String s) {
		return decodeMessage(s, null);
	}

	public String encode(M message) throws JOSEException, Exception {
		return encodeMessage(message, null).serialize();
	}

	@Override
	protected M readInternal(Class<? extends M> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		log.trace("Deserialize {}", clazz.getSimpleName());
		String s = StreamUtils.copyToString(inputMessage.getBody(), UTF8);
		if (MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(inputMessage.getHeaders().getContentType())) {
			s = URLDecoder.decode(s, UTF8);
			if (s.length() > 0 && s.charAt(s.length() - 1) == '=') {
				s = s.substring(0, s.length() - 1);
			}
		}
		return decodeMessage(s, inputMessage);
	}

	/**
	 * @param s
	 * @param inputMessage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected M decodeMessage(String s, HttpInputMessage inputMessage) {
		try {
			Message<?> message = service.unwrapMessage(s, this.validator::validate, getDecoder(inputMessage));
			if (!getMessageClass().isInstance(message) || !getBodyClass().isInstance(message.getBody())) {
				throw new ParseException("Message must be " + getMessageClass().getSimpleName() + " and body must be "
						+ getBodyClass().getSimpleName(), 0);
			}
			return (M) message;
		} catch (Exception e) {
			log.trace("Exception", e);
			throw new HttpMessageNotReadableException("Unable to parse Message", e, inputMessage);
		}
	}

	/**
	 * @param inputMessage
	 * @return
	 */
	protected abstract FunctionWithException<Map<String, Object>, ?, Exception> getDecoder(HttpInputMessage inputMessage);

	@Override
	protected void writeInternal(M t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		log.trace("Serialize {}", t.getClass().getSimpleName());
		try {
			JWSObject wrapped = encodeMessage(t, outputMessage);
			StreamUtils.copy(wrapped.serialize(), UTF8, outputMessage.getBody());
		} catch (Exception e) {
			log.trace("Exception", e);
			throw new HttpMessageNotWritableException("Unable to sign/serialize Message", e);
		}
	}

	/**
	 * @param t
	 * @param outputMessage
	 * @return
	 * @throws Exception
	 * @throws JOSEException
	 */
	protected JWSObject encodeMessage(M t, HttpOutputMessage outputMessage) throws Exception, JOSEException {
		JWSObject wrapped = service.wrap(t, getEncoder(outputMessage));
		return validator.sign(wrapped, keys.getSigner(wrapped.getHeader().getAlgorithm(), t.getIssuer(), t.getKeyId()));
	}

	/**
	 * @param outputMessage
	 * @return
	 */
	protected abstract FunctionWithException<T, Map<String, Object>, Exception> getEncoder(HttpOutputMessage outputMessage);

}
