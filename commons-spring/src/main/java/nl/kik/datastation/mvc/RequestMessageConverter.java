package nl.kik.datastation.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSObject;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.Request;
import nl.kik.datastation.dto.vc.VerifiableBase;
import nl.kik.datastation.dto.vc.VerifiableCredential;
import nl.kik.datastation.dto.vc.VerifiablePresentation;
import nl.kik.datastation.service.VerifiableCredentialService;
import nl.kik.datastation.util.FunctionWrapper.BiFunctionWithException;
import nl.kik.datastation.util.FunctionWrapper.FunctionWithException;

public class RequestMessageConverter
		extends MessageMessageConverter<VerifiablePresentation, Request<VerifiablePresentation>> {
	@Autowired
	private VerifiableCredentialService vcService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<Request<VerifiablePresentation>> getMessageClass() {
		return (Class) Request.class;
	}

	@Override
	protected Class<VerifiablePresentation> getBodyClass() {
		return VerifiablePresentation.class;
	}

	@Override
	protected FunctionWithException<JSONObject, VerifiableBase, Exception> getDecoder(HttpInputMessage inputMessage) {
		return service.base64Unwrapper(ss -> vcService.unwrapVerifiable(ss, this.validator::validate));
	}

	@Override
	protected FunctionWithException<VerifiablePresentation, JSONObject, Exception> getEncoder(
			HttpOutputMessage outputMessage) {
		BiFunctionWithException<VerifiableCredential, JWSObject, JWSObject, Exception> signer = //
				(c, w) -> vcService.sign(keys.getSigner(w.getHeader().getAlgorithm(), c.getIssuer(), c.getKeyId()))
						.apply(w);
		FunctionWithException<VerifiablePresentation, JWSObject, Exception> wrapper = //
				vcService.wrapAndSign(v -> keys.getSigner(JWSAlgorithm.EdDSA, v.getIssuer(), v.getKeyId()), signer);
		return service.base64Wrapper(wrapper);
	}

}
