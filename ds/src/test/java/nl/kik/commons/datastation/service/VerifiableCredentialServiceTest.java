package nl.kik.commons.datastation.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.ds.async.Request;
import nl.kik.commons.datastation.dto.vc.AbstractVerifiableCredentialTest;
import nl.kik.commons.datastation.dto.vc.VerifiableBase;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
import nl.kik.commons.datastation.util.FunctionWrapper;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class VerifiableCredentialServiceTest extends AbstractVerifiableCredentialTest {
	private VerifiableCredentialService service;
	private OctetKeyPair jwk, centralJwk;
	private JWSSigner signer, centralSigner;

	@BeforeAll
	void setUpKey() throws JOSEException {
		jwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:userkey") //
				.generate();
		signer = new Ed25519Signer(jwk);

		centralJwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:centralkey") //
				.generate();
		centralSigner = new Ed25519Signer(centralJwk);
	}

	@BeforeEach
	void setUpService() throws Exception {
		service = new VerifiableCredentialService();
	}

	JWSObject sign(final JWSObject o, final JWSSigner s) {
		try {
			o.sign(s);
			return o;
		} catch (final JOSEException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testLoad() throws ParseException, MalformedURLException, JOSEException {
		for (final VerifiableBase m : messages) {
			VerifiableCredentialServiceTest.log.info("Comparing");
			VerifiableCredentialServiceTest.log.info("{}", m);
			final JWSObject wrapped = service.wrap(m, (c, w) -> sign(w, centralSigner));
			wrapped.sign(signer);
			final String flat = wrapped.serialize();
			final VerifiableBase unwrapped = service.unwrapVerifiable(flat, (w, v) -> {
			});
			VerifiableCredentialServiceTest.log.info("{}", unwrapped);
			Assertions.assertEquals(m, unwrapped);
		}
	}

	@Test
	void testSave() {
		messages.forEach(FunctionWrapper.wrapper(m -> {
			final JWSObject wrapped = service.wrap(m, (c, w) -> sign(w, centralSigner));
			wrapped.sign(signer);
			System.out.println("Header: " + wrapped.getHeader().toJSONObject());
			System.out.println("Payload: " + wrapped.getPayload().toJSONObject());
			System.out.println("Base64: " + wrapped.serialize());
		}));
	}

	@Test
	void testWrapInMessage() throws Exception {
		final Request<VerifiablePresentation> message = Request.<VerifiablePresentation>builder() //
				.body(presentation) //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, AbstractVerifiableCredentialTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, AbstractVerifiableCredentialTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.threadId("urn:thread") //
				.replyUrl(new URL("http://example.com/service/reply")) //
				.build();
		final MessageService messageService = new MessageService();

		final JWSObject wrapped = messageService.wrap(message,
				messageService.base64Wrapper(service.wrapAndSign(this::sign, c -> signer, (c, w) -> sign(w, centralSigner))));
		wrapped.sign(signer);
		System.out.println(wrapped.serialize());
	}

}
