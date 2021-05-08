package nl.kik.datastation.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;

public interface KeyService {
	JWSSigner getSigner(String keyId) throws JOSEException;
	JWSVerifier getVerifier(String keyId) throws JOSEException;
}
