package nl.kik.datastation.service;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;

import nl.kik.datastation.dto.Token;

public interface ValidationService {
	void validate(JWSObject jws, Token t) throws Exception;
	JWSObject sign(JWSObject object, JWSSigner signer) throws Exception;
}
