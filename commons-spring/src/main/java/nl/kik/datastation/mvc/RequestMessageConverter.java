package nl.kik.datastation.mvc;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.nimbusds.jose.JWSObject;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.ds.async.Request;
import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.dto.vc.VerifiableCredential;
import nl.kik.datastation.dto.vc.VerifiablePresentation;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.MessageService;
import nl.kik.datastation.service.ValidationService;
import nl.kik.datastation.service.VerifiableCredentialService;
import nl.kik.datastation.util.FunctionWrapper.BiFunctionWithException;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

public class RequestMessageConverter extends AbstractHttpMessageConverter<Request<VerifiablePresentation>> {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	@Autowired
	private VerifiableCredentialService vcService;
	@Autowired
	private MessageService service;
	@Autowired
	private KeyService keys;
	@Autowired
	private ValidationService validator;

	public RequestMessageConverter() {
		super(MediaType.ALL);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return Request.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Request<VerifiablePresentation> readInternal(Class<? extends Request<VerifiablePresentation>> clazz,
			HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		String s = StreamUtils.copyToString(inputMessage.getBody(), UTF8);
		if (MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(inputMessage.getHeaders().getContentType())) {
			s = URLDecoder.decode(s, UTF8);
			if (s.length() > 0 && s.charAt(s.length() - 1) == '=') {
				s = s.substring(0, s.length() - 1);
			}
		}
		try {
			FunctionWithException<JSONObject, VerifiableBase, Exception> decoder = //
					service.base64Unwrapper(ss -> vcService.unwrapVerifiable(ss,
							(j, v) -> this.validator.validate(j, v, inputMessage)));
			Message<VerifiableBase> message = service.unwrapMessage(s,
					(j, v) -> this.validator.validate(j, v, inputMessage), decoder);
			if (!(message instanceof Request<?>) || !(message.getBody() instanceof VerifiablePresentation)) {
				throw new ParseException("Message must be Request and body must be a VP", 0);
			}
			return (Request<VerifiablePresentation>) (Request) message;
		} catch (Exception e) {
			throw new HttpMessageNotReadableException("Unable to parse Message", e, inputMessage);
		}
	}

	@Override
	protected void writeInternal(Request<VerifiablePresentation> t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		try {
			BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, Exception> signer = //
					(c, w) -> vcService.sign(keys.getSigner(c.getKeyId())).apply(w);
			FunctionWithException<VerifiablePresentation, JWSObject, Exception> wrapper = //
					vcService.wrapAndSign(v -> keys.getSigner(v.getKeyId()), signer);
			JWSObject wrapped = service.wrap(t, service.base64Wrapper(wrapper));
			wrapped.sign(keys.getSigner(wrapped.getHeader().getKeyID()));
			StreamUtils.copy(wrapped.serialize(), UTF8, outputMessage.getBody());
		} catch (Exception e) {
			throw new HttpMessageNotWritableException("Unable to sign/serialize Message", e);
		}
	}

}
