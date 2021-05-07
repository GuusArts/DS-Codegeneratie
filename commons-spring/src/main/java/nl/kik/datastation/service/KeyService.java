package nl.kik.datastation.service;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

@Service
public class KeyService {
	private OctetKeyPair jwk;
	private Ed25519Signer signer;

	@PostConstruct
	public void init() throws JOSEException {
		jwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:userkey") //
				.generate();
		signer = new Ed25519Signer(jwk);

	}

	public JWSSigner getSigner(String keyId) {
		return signer;
	}

}
