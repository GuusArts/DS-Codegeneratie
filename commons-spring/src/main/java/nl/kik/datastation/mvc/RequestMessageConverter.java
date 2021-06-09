package nl.kik.datastation.mvc;

import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;

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

public class RequestMessageConverter
		extends MessageMessageConverter<VerifiablePresentation, Request<VerifiablePresentation>> {
	private final VerifiableCredentialService vcService;

	public RequestMessageConverter(final MessageService service, final VerifiableCredentialService vcService,
			final KeyService keys, final ValidationService validator) {
		super(service, keys, validator);
		this.vcService = vcService;
	}

	@Override
	protected Class<VerifiablePresentation> getBodyClass() {
		return VerifiablePresentation.class;
	}

	@Override
	protected FunctionWithException<Map<String, Object>, VerifiableBase, Exception> getDecoder(
			final HttpInputMessage inputMessage) {
		return service.base64Unwrapper(ss -> vcService.unwrapVerifiable(ss, validator::validate));
	}

	@Override
	protected FunctionWithException<VerifiablePresentation, Map<String, Object>, Exception> getEncoder(
			final HttpOutputMessage outputMessage) {
		final BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, Exception> signer = //
				(c, w) -> validator.sign(w, keys.getSigner(w.getHeader().getAlgorithm(), c.getIssuer(), c.getKeyId()));
		final FunctionWithException<VerifiablePresentation, JWSObject, Exception> wrapper = //
				vcService.wrapAndSign(validator::sign,
						v -> keys.getSigner(JWSAlgorithm.EdDSA, v.getIssuer(), v.getKeyId()), signer);
		return service.base64Wrapper(wrapper);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<Request<VerifiablePresentation>> getMessageClass() {
		return (Class) Request.class;
	}

}
