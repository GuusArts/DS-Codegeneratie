package nl.kik.datastation.service;

import org.springframework.http.HttpInputMessage;

import com.nimbusds.jose.JWSObject;

import nl.kik.datastation.dto.Token;

public interface ValidationService {
	void validate(JWSObject jws, Token t, HttpInputMessage message) throws Exception;
}
