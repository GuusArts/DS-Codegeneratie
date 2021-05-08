package nl.kik.datastation.service;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.vc.VerifiableBase;

@Slf4j
public class DefaultValidationService implements ValidationService {
	@Autowired
	private KeyService keys;
	@Autowired
	private VerifiableCredentialService service;

	@Override
	public void validate(JWSObject jws, VerifiableBase vc, HttpInputMessage headers)
			throws ParseException, JOSEException {
		log.info("Validating {} (from {} received as {})", vc, jws, headers);
		validateSignature(keys.getVerifier(jws.getHeader().getKeyID()), jws);
		service.validateFields(vc);
		service.validateIntegrity(vc);
	}

	protected void validateSignature(JWSVerifier verifier, JWSObject jws) throws ParseException, JOSEException {
		log.info("Validating signature using {}", jws.getHeader().getKeyID());
		if (verifier == null) {
			throw new ParseException("Key " + jws.getHeader().getKeyID() + " could not be found for validation", 0);
		}
		if (!jws.verify(verifier)) {
			throw new ParseException("Signature did not match", 0);
		}
	}
}
