package nl.kik.commons.datastation.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;

public interface KeyService {
	JWSSigner getSigner(JWSAlgorithm jwsAlgorithm, String issuer, String keyId) throws JOSEException;

	JWSVerifier getVerifier(JWSAlgorithm jwsAlgorithm, String issuer, String keyId) throws JOSEException;
}
