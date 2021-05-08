package nl.kik.datastation.mvc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;

import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.ValidationService;
import nl.kik.datastation.service.VerifiableCredentialService;

public class VerifiableCredentialMessageConverter extends AbstractHttpMessageConverter<VerifiableBase> {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	@Autowired
	private VerifiableCredentialService service;
	@Autowired
	private KeyService keys;
	@Autowired
	private ValidationService validator;

	public VerifiableCredentialMessageConverter() {
		super(MediaType.ALL);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return VerifiableBase.class.isAssignableFrom(clazz);
	}

	@Override
	protected VerifiableBase readInternal(Class<? extends VerifiableBase> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		String s = StreamUtils.copyToString(inputMessage.getBody(), UTF8);
		try {
			return service.unwrapVerifiable(s, (o, v) -> validator.validate(o, v, inputMessage));
		} catch (Exception e) {
			throw new HttpMessageNotReadableException("Unable to parse VC", e, inputMessage);
		}
	}

	@Override
	protected void writeInternal(VerifiableBase t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		try {
			JWSObject o = service.wrap(t, (c, w) -> service.sign(keys.getSigner(c.getKeyId())).apply(w));
			o.sign(keys.getSigner(t.getKeyId()));
			StreamUtils.copy(o.serialize(), Charset.forName("UTF-8"), outputMessage.getBody());
		} catch (JOSEException | IllegalArgumentException e) {
			throw new HttpMessageNotWritableException("Unable to sign/serialize VC", e);
		}
	}

}
