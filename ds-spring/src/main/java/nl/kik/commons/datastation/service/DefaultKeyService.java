package nl.kik.commons.datastation.service;

import jakarta.annotation.PostConstruct;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

public class DefaultKeyService implements KeyService {
	private OctetKeyPair jwk;
	protected JWK publicJwk;
	private JWSSigner signer;
	private JWSVerifier verifier;

	@Override
	public JWSSigner getSigner(final JWSAlgorithm jwsAlgorithm, final String issuer, final String keyId) {
		return signer;
	}

	@Override
	public JWSVerifier getVerifier(final JWSAlgorithm jwsAlgorithm, final String issuer, final String keyId)
			throws JOSEException {
		return verifier;
	}

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
}
