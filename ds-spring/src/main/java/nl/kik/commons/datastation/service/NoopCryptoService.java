package nl.kik.commons.datastation.service;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;

@Slf4j
public class NoopCryptoService implements CryptoService {
	private OctetKeyPair jwk;
	protected JWK publicJwk;
	private JWSSigner signer;
	private ObjectMapper mapper;

	@PostConstruct
	public void init() throws JOSEException {
		jwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:userkey") //
				.generate();
		signer = new Ed25519Signer(jwk);
		final OctetKeyPair publicKey = jwk.toPublicJWK();
		publicJwk = publicKey;
		mapper = new ObjectMapper();
	}

	@Override
	public JWSObject sign(ResultSet value) throws Exception {
		log.trace("Signing {}", value);
		String serialized = mapper.writeValueAsString(value);
		Payload payload = new Payload(serialized);
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.EdDSA) //
				.keyID(jwk.getKeyID()) //
				.build();
		JWSObject jws = new JWSObject(header, payload);
		jws.sign(signer);
		return jws;
	}

	@Override
	public ResultSet validate(JWSObject value) throws Exception {
		log.warn("Not actually validating result; please only use {} for testing",
				NoopCryptoService.class.getSimpleName());
		return mapper.readValue(value.getPayload().toBytes(), ResultSet.class);
	}

	@Override
	public void check(VerifiablePresentation vp) throws Exception {
		log.warn("Not actually validating vp; please only use {} for testing", NoopCryptoService.class.getSimpleName());
	}

}
