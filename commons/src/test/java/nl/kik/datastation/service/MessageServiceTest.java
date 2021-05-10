package nl.kik.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Map;

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

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.AbstractMessageTest;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.util.FunctionWrapper;

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
	void testSave() {
		messages.forEach(FunctionWrapper.wrapper(m -> {
			JWSObject wrapped = service.wrap(m, s -> new JSONObject(Map.of("plain", s)));
			wrapped.sign(signer);
			System.out.println("Header: " + wrapped.getHeader().toJSONObject());
			System.out.println("Payload: " + wrapped.getPayload().toJSONObject());
			System.out.println("Base64: " + wrapped.serialize());
		}));
	}

	@Test
	void testLoad() throws ParseException, MalformedURLException, JOSEException {
		for (Message<String> m : messages) {
			JWSObject wrapped = service.wrap(m, s -> new JSONObject(Map.of("plain", s)));
			wrapped.sign(signer);
			String serialized = wrapped.serialize();
			Message<String> unwrapped = service.unwrapMessage(serialized, (j, o) -> {
			}, o -> o.getAsString("plain"));
			log.trace("Comparing");
			log.trace("{}", m);
			log.trace("{}", unwrapped);
			assertEquals(m, unwrapped);
		}
	}

}
