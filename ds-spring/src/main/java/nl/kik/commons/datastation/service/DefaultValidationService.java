package nl.kik.commons.datastation.service;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.Token;
import nl.kik.commons.datastation.dto.ds.async.Message;
import nl.kik.commons.datastation.dto.vc.VerifiableBase;

@Slf4j
public class DefaultValidationService<T> implements ValidationService {
	@Autowired
	protected KeyService keys;
	@Autowired
	protected VerifiableCredentialService vcService;
	@Autowired
	protected MessageService messageService;

	@Override
	public JWSObject sign(final JWSObject object, final JWSSigner signer) throws Exception {
		if (signer == null)
			throw new ParseException("Trying to sign object without a key", 0);
		object.sign(signer);
		return object;
	}

	protected <U> void validate(final JWSObject jws, final Message<U> t, final JWTClaimsSet claims, final T aux)
			throws Exception {
		validateSignature(
				keys.getVerifier(jws.getHeader().getAlgorithm(), claims.getIssuer(), jws.getHeader().getKeyID()), jws,
				claims);
		messageService.validateFields(t);
		messageService.validateIntegrity(t, (m, b) -> validationDelegate(m, b, aux));
	}

	@Override
	public void validate(final JWSObject jws, final Token t) throws Exception {
		validate(jws, t, null);
	}

	protected void validate(final JWSObject jws, final Token t, final T aux) throws Exception {
		final JWTClaimsSet claims = JWTClaimsSet.parse(jws.getPayload().toJSONObject());
		DefaultValidationService.log.trace("Validating {} (from {} received as {})", t, jws);
		if (t instanceof Message<?>) {
			validate(jws, (Message<?>) t, claims, aux);
		} else if (t instanceof VerifiableBase) {
			validate(jws, (VerifiableBase) t, claims, aux);
		} else {
			validateExtension(jws, t, claims, aux);
		}
	}

	protected void validate(final JWSObject jws, final VerifiableBase t, final JWTClaimsSet claims, final T aux)
			throws Exception {
		validateSignature(
				keys.getVerifier(jws.getHeader().getAlgorithm(), claims.getIssuer(), jws.getHeader().getKeyID()), jws,
				claims);
		vcService.validateFields(t);
		vcService.validateIntegrity(t);
	}

	protected void validateExtension(final JWSObject jws, final Token t, final JWTClaimsSet claims, final T aux)
			throws Exception {
		throw new ParseException("Received object of unexpected type " + t.getClass().getCanonicalName(), 0);
	}

	protected void validateSignature(final JWSVerifier verifier, final JWSObject jws, final JWTClaimsSet claims)
			throws Exception {
		DefaultValidationService.log.trace("Validating signature using {}", jws.getHeader().getKeyID());
		if (verifier == null)
			throw new ParseException("Key " + jws.getHeader().getKeyID() + " could not be found for validation", 0);
		if (!jws.verify(verifier))
			throw new ParseException("Signature did not match", 0);
	}

	protected <U> void validationDelegate(final Message<U> m, final U body, final T aux) throws Exception {
	}
}
