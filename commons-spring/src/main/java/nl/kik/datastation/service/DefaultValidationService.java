package nl.kik.datastation.service;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.Token;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.vc.VerifiableBase;

@Slf4j
public class DefaultValidationService implements ValidationService {
	@Autowired
	private KeyService keys;
	@Autowired
	private VerifiableCredentialService vcService;
	@Autowired
	private MessageService messageService;

	@Override
	public void validate(JWSObject jws, Token t, HttpInputMessage headers) throws Exception {
		log.trace("Validating {} (from {} received as {})", t, jws, headers);
		if (t instanceof Message<?>) {
			validate(jws, (Message<?>) t, headers);
		} else if (t instanceof VerifiableBase) {
			validate(jws, (VerifiableBase) t, headers);
		} else {
			validateExtension(jws, t, headers);
		}
	}

	private void validateExtension(JWSObject jws, Token t, HttpInputMessage headers) throws Exception {
		throw new ParseException("Received object of unexpected type " + t.getClass().getCanonicalName(), 0);
	}

	protected <T> void validate(JWSObject jws, Message<T> t, HttpInputMessage headers) throws Exception {
		validateSignature(keys.getVerifier(jws.getHeader().getKeyID()), jws);
		messageService.validateFields(t);
		messageService.validateIntegrity(t, (m, b) -> validationDelegate(m, b));
	}

	protected void validate(JWSObject jws, VerifiableBase t, HttpInputMessage headers) throws Exception {
		validateSignature(keys.getVerifier(jws.getHeader().getKeyID()), jws);
		vcService.validateFields(t);
		vcService.validateIntegrity(t);
	}

	protected <T> void validationDelegate(Message<T> m, T body) throws Exception {
	}

	protected void validateSignature(JWSVerifier verifier, JWSObject jws) throws Exception {
		log.trace("Validating signature using {}", jws.getHeader().getKeyID());
		if (verifier == null) {
			throw new ParseException("Key " + jws.getHeader().getKeyID() + " could not be found for validation", 0);
		}
		if (!jws.verify(verifier)) {
			throw new ParseException("Signature did not match", 0);
		}
	}
}
