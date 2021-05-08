package nl.kik.datastation.service;

import java.text.ParseException;

import org.springframework.http.HttpInputMessage;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;

import nl.kik.datastation.dto.vc.VerifiableBase;

public interface ValidationService {
	void validate(JWSObject jws, VerifiableBase vc, HttpInputMessage message)
			throws ParseException, JOSEException;
}
