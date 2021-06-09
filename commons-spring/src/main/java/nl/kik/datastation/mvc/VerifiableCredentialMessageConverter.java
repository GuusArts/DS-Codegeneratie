package nl.kik.datastation.mvc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import com.nimbusds.jose.JWSObject;

import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.service.KeyService;
import nl.kik.datastation.service.ValidationService;
import nl.kik.datastation.service.VerifiableCredentialService;

public class VerifiableCredentialMessageConverter extends AbstractHttpMessageConverter<VerifiableBase> {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private final VerifiableCredentialService service;
	private final KeyService keys;
	private final ValidationService validator;

	public VerifiableCredentialMessageConverter(final VerifiableCredentialService service, final KeyService keys,
			final ValidationService validator) {
		super(MediaType.ALL);
		this.service = service;
		this.keys = keys;
		this.validator = validator;
	}

	@Override
	protected VerifiableBase readInternal(final Class<? extends VerifiableBase> clazz,
			final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		final String s = StreamUtils.copyToString(inputMessage.getBody(), VerifiableCredentialMessageConverter.UTF8);
		try {
			return service.unwrapVerifiable(s, validator::validate);
		} catch (final Exception e) {
			throw new HttpMessageNotReadableException("Unable to parse VC", e, inputMessage);
		}
	}

	@Override
	protected boolean supports(final Class<?> clazz) {
		return VerifiableBase.class.isAssignableFrom(clazz);
	}

	@Override
	protected void writeInternal(final VerifiableBase t, final HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		try {
			final JWSObject o = service.wrap(t, (c, w) -> validator.sign(w,
					keys.getSigner(w.getHeader().getAlgorithm(), c.getIssuer(), c.getKeyId())));
			validator.sign(o, keys.getSigner(o.getHeader().getAlgorithm(), t.getIssuer(), t.getKeyId()));
			StreamUtils.copy(o.serialize(), Charset.forName("UTF-8"), outputMessage.getBody());
		} catch (final Exception e) {
			throw new HttpMessageNotWritableException("Unable to sign/serialize VC", e);
		}
	}

}
