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

	public MessageMessageConverter(final MessageService service, final KeyService keys,
			final ValidationService validator) {
		super(MediaType.ALL);
		this.service = service;
		this.keys = keys;
		this.validator = validator;
	}

	public M decode(final String s) {
		return decodeMessage(s, null);
	}

	/**
	 * @param s
	 * @param inputMessage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected M decodeMessage(final String s, final HttpInputMessage inputMessage) {
		try {
			final Message<?> message = service.unwrapMessage(s, this.validator::validate, getDecoder(inputMessage));
			if (!getMessageClass().isInstance(message) || !getBodyClass().isInstance(message.getBody()))
				throw new ParseException("Message must be " + getMessageClass().getSimpleName() + " and body must be "
						+ getBodyClass().getSimpleName(), 0);
			return (M) message;
		} catch (final Exception e) {
			MessageMessageConverter.log.trace("Exception", e);
			throw new HttpMessageNotReadableException("Unable to parse Message", e, inputMessage);
		}
	}

	public String encode(final M message) throws JOSEException, Exception {
		return encodeMessage(message, null).serialize();
	}

	/**
	 * @param t
	 * @param outputMessage
	 * @return
	 * @throws Exception
	 * @throws JOSEException
	 */
	protected JWSObject encodeMessage(final M t, final HttpOutputMessage outputMessage)
			throws Exception, JOSEException {
		final JWSObject wrapped = service.wrap(t, getEncoder(outputMessage));
		return validator.sign(wrapped, keys.getSigner(wrapped.getHeader().getAlgorithm(), t.getIssuer(), t.getKeyId()));
	}

	protected abstract Class<T> getBodyClass();

	/**
	 * @param inputMessage
	 * @return
	 */
	protected abstract FunctionWithException<Map<String, Object>, ?, Exception> getDecoder(
			HttpInputMessage inputMessage);

	/**
	 * @param outputMessage
	 * @return
	 */
	protected abstract FunctionWithException<T, Map<String, Object>, Exception> getEncoder(
			HttpOutputMessage outputMessage);

	protected abstract Class<M> getMessageClass();

	@Override
	protected M readInternal(final Class<? extends M> clazz, final HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		MessageMessageConverter.log.trace("Deserialize {}", clazz.getSimpleName());
		String s = StreamUtils.copyToString(inputMessage.getBody(), MessageMessageConverter.UTF8);
		if (MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(inputMessage.getHeaders().getContentType())) {
			s = URLDecoder.decode(s, MessageMessageConverter.UTF8);
			if (s.length() > 0 && s.charAt(s.length() - 1) == '=') {
				s = s.substring(0, s.length() - 1);
			}
		}
		return decodeMessage(s, inputMessage);
	}

	@Override
	protected boolean supports(final Class<?> clazz) {
		return getMessageClass().isAssignableFrom(clazz);
	}

	@Override
	protected void writeInternal(final M t, final HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		MessageMessageConverter.log.trace("Serialize {}", t.getClass().getSimpleName());
		try {
			final JWSObject wrapped = encodeMessage(t, outputMessage);
			StreamUtils.copy(wrapped.serialize(), MessageMessageConverter.UTF8, outputMessage.getBody());
		} catch (final Exception e) {
			MessageMessageConverter.log.trace("Exception", e);
			throw new HttpMessageNotWritableException("Unable to sign/serialize Message", e);
		}
	}

}
