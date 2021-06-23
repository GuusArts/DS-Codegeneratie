package nl.kik.commons.datastation.service;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.util.JSONObjectUtils;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.ds.async.AbstractMessageTest;
import nl.kik.commons.datastation.dto.ds.async.Message;
import nl.kik.commons.datastation.util.FunctionWrapper;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class MessageServiceTest extends AbstractMessageTest {
	private MessageService service;

	private OctetKeyPair jwk;
	private Ed25519Signer signer;

	@BeforeAll
	void setUpKey() throws JOSEException {
		jwk = new OctetKeyPairGenerator(Curve.Ed25519) //
				.keyID("urk:userkey") //
				.generate();
		signer = new Ed25519Signer(jwk);
	}

	@BeforeEach
	void setUpService() throws Exception {
		service = new MessageService();
	}

	@Test
	void testLoad() throws ParseException, MalformedURLException, JOSEException {
		for (final Message<String> m : messages) {
			final JWSObject wrapped = service.wrap(m, s -> Map.of("plain", s));
			wrapped.sign(signer);
			final String serialized = wrapped.serialize();
			final Message<String> unwrapped = service.unwrapMessage(serialized, (j, o) -> {
			}, o -> JSONObjectUtils.getString(o, "plain"));
			MessageServiceTest.log.trace("Comparing");
			MessageServiceTest.log.trace("{}", m);
			MessageServiceTest.log.trace("{}", unwrapped);
			Assertions.assertEquals(m, unwrapped);
		}
	}

	@Test
	void testSave() {
		messages.forEach(FunctionWrapper.wrapper(m -> {
			final JWSObject wrapped = service.wrap(m, s -> Map.of("plain", s));
			wrapped.sign(signer);
			System.out.println("Header: " + wrapped.getHeader().toJSONObject());
			System.out.println("Payload: " + wrapped.getPayload().toJSONObject());
			System.out.println("Base64: " + wrapped.serialize());
		}));
	}

}
