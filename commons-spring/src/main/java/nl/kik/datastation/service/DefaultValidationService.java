package nl.kik.datastation.service;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.Token;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.vc.VerifiableBase;

@Slf4j
public class DefaultValidationService<T> implements ValidationService {
	@Autowired
	protected KeyService keys;
	@Autowired
	protected VerifiableCredentialService vcService;
	@Autowired
	protected MessageService messageService;

	@Override
	public void validate(JWSObject jws, Token t) throws Exception {
		validate(jws, t, null);
	}

	protected void validate(JWSObject jws, Token t, T aux) throws Exception {
		JWTClaimsSet claims = JWTClaimsSet.parse(jws.getPayload().toJSONObject());
		log.trace("Validating {} (from {} received as {})", t, jws);
		if (t instanceof Message<?>) {
			validate(jws, (Message<?>) t, claims, aux);
		} else if (t instanceof VerifiableBase) {
			validate(jws, (VerifiableBase) t, claims, aux);
		} else {
			validateExtension(jws, t, claims, aux);
		}
	}

	protected void validateExtension(JWSObject jws, Token t, JWTClaimsSet claims, T aux) throws Exception {
		throw new ParseException("Received object of unexpected type " + t.getClass().getCanonicalName(), 0);
	}

	protected <U> void validate(JWSObject jws, Message<U> t, JWTClaimsSet claims, T aux) throws Exception {
		validateSignature(
				keys.getVerifier(jws.getHeader().getAlgorithm(), claims.getIssuer(), jws.getHeader().getKeyID()), jws,
				claims);
		messageService.validateFields(t);
		messageService.validateIntegrity(t, (m, b) -> validationDelegate(m, b, aux));
	}

	protected void validate(JWSObject jws, VerifiableBase t, JWTClaimsSet claims, T aux) throws Exception {
		validateSignature(
				keys.getVerifier(jws.getHeader().getAlgorithm(), claims.getIssuer(), jws.getHeader().getKeyID()), jws,
				claims);
		vcService.validateFields(t);
		vcService.validateIntegrity(t);
	}

	protected <U> void validationDelegate(Message<U> m, U body, T aux) throws Exception {
	}

	protected void validateSignature(JWSVerifier verifier, JWSObject jws, JWTClaimsSet claims) throws Exception {
		log.trace("Validating signature using {}", jws.getHeader().getKeyID());
		if (verifier == null) {
			throw new ParseException("Key " + jws.getHeader().getKeyID() + " could not be found for validation", 0);
		}
		if (!jws.verify(verifier)) {
			throw new ParseException("Signature did not match", 0);
		}
	}
}
