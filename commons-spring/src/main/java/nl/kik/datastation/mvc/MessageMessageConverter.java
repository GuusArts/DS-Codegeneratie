package nl.kik.datastation.mvc;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSObject;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.dto.vc.VerifiableCredential;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.MessageService;
import nl.kik.datastation.service.ValidationService;
import nl.kik.datastation.service.VerifiableCredentialService;
import nl.kik.datastation.util.FunctionWrapper.BiConsumerWithException;
import nl.kik.datastation.util.FunctionWrapper.BiFunctionWithException;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

public class MessageMessageConverter<T extends VerifiableBase> extends AbstractHttpMessageConverter<Message<T>> {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	@Autowired
	private VerifiableCredentialService vcService;
	@Autowired
	private MessageService service;
	@Autowired
	private KeyService keys;
	@Autowired
	private ValidationService validator;

	public MessageMessageConverter() {
		super(MediaType.ALL);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return Message.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Message<T> readInternal(Class<? extends Message<T>> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		String s = StreamUtils.copyToString(inputMessage.getBody(), UTF8);
		if (MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(inputMessage.getHeaders().getContentType())) {
			s = URLDecoder.decode(s, UTF8);
			if (s.length() > 0 && s.charAt(s.length() - 1) == '=') {
				s = s.substring(0, s.length() - 1);
			}
		}
		try {
			BiConsumerWithException<JWSObject, VerifiableBase, Exception> validator = //
					(j, v) -> this.validator.validate(j, v, inputMessage);
			FunctionWithException<JSONObject, VerifiableBase, Exception> decoder = //
					service.base64Unwrapper(ss -> vcService.unwrapVerifiable(ss, validator));
			Message<VerifiableBase> message = service.unwrapMessage(s, decoder);
			service.validateFields(message);
			service.validateIntegrity(message, vcService::validateMessageIntegrity);
			return (Message<T>) (Message) message;
		} catch (Exception e) {
			throw new HttpMessageNotReadableException("Unable to parse Message", e, inputMessage);
		}
	}

	@Override
	protected void writeInternal(Message<T> t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		try {
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, Exception> signer = //
					(c, w) -> vcService.sign(keys.getSigner(c.getKeyId())).apply(w);
			FunctionWithException<T, JWSObject, Exception> wrapper = //
					vcService.wrapAndSign(v -> keys.getSigner(v.getKeyId()), signer);
			JOSEObject wrapped = service.wrap(t, service.base64Wrapper(wrapper));
			JSONObject serialized = service.serialize(wrapped);
			StreamUtils.copy(serialized.toString(), UTF8, outputMessage.getBody());
		} catch (Exception e) {
			throw new HttpMessageNotWritableException("Unable to sign/serialize Message", e);
		}
	}

}
