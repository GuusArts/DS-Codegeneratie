package nl.kik.datastation.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.ds.async.Request;
import nl.kik.datastation.dto.vc.AbstractVerifiableCredentialTest;
import nl.kik.datastation.dto.vc.VerifiablePresentation;
import nl.kik.datastation.util.FunctionWrapper;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class VerifiableCredentialServiceTest extends AbstractVerifiableCredentialTest {
	private VerifiableCredentialService service;
	private OctetKeyPair jwk;
	private OctetKeyPair publicJWK;
	private JWSSigner signer;

	@BeforeAll
	void setUpKey() throws JOSEException {
		jwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:userkey") //
				.generate();
		publicJWK = jwk.toPublicJWK();
		signer = new Ed25519Signer(jwk);
	}

	@BeforeEach
	void setUpService() throws Exception {
		service = new VerifiableCredentialService();
	}

	@Test
	void testSave() {
		messages.forEach(FunctionWrapper.wrapper(m -> {
			JWSObject wrapped = service.wrap(m, (c, w) -> service.sign(signer).apply(w));
			wrapped.sign(signer);
			System.out.println("Header: " + wrapped.getHeader().toJSONObject());
			System.out.println("Payload: " + wrapped.getPayload().toJSONObject());
			System.out.println("Base64: " + wrapped.serialize());
		}));
	}

	@Test
	void testWrapInMessage() throws Exception {
		OctetKeyPair centralJwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:centralkey") //
				.generate();
		Ed25519Signer centralSigner = new Ed25519Signer(centralJwk);

		Request<VerifiablePresentation> message = Request.<VerifiablePresentation>builder() //
				.body(presentation) //
				.build();
		MessageService messageService = new MessageService();
		JOSEObject wrapped = messageService.wrap(message,
				messageService.base64Wrapper(service.wrapAndSign(signer, (c, w) -> service.sign(centralSigner).apply(w))));
		System.out.println(messageService.serialize(wrapped).toString());
	}
//
//	@Test
//	void testLoad() throws ParseException, MalformedURLException {
//		for (Message<String> m : messages) {
//			JOSEObject wrapped = service.wrap(m, s -> new JSONObject(Map.of("plain", s)));
//			JSONObject serialized = service.serialize(wrapped);
//			String flat = serialized.toString();
//			Message<String> unwrapped = service.unwrapMessage(flat, o -> o.getAsString("plain"));
//			log.trace("Comparing");
//			log.trace("{}", m);
//			log.trace("{}", unwrapped);
//			assertEquals(m, unwrapped);
//		}
//	}

}
