package nl.kik.commons.datastation.service;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.kikv.ResultSet;

@Slf4j
public class NoopValidationService implements ValidationService {
	private OctetKeyPair jwk;
	protected JWK publicJwk;
	private JWSSigner signer;
	private JWSVerifier verifier;

	@PostConstruct
	public void init() throws JOSEException {
		jwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:userkey") //
				.generate();
		signer = new Ed25519Signer(jwk);
		final OctetKeyPair publicKey = jwk.toPublicJWK();
		verifier = new Ed25519Verifier(publicKey);
		publicJwk = publicKey;
	}

	@Override
	public JWSObject sign(ResultSet value) throws Exception {
		log.info("Signing {}", value);
		ObjectMapper mapper = new ObjectMapper();
		String serialized = mapper.writeValueAsString(value);
		Payload payload = new Payload(serialized);
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.EdDSA) //
				.keyID(jwk.getKeyID()) //
				.build();
		JWSObject jws = new JWSObject(header, payload);
		jws.sign(signer);
		return jws;
	}

}
