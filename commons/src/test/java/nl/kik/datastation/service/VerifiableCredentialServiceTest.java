package nl.kik.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JOSEObject;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.async.AbstractMessageTest;
import nl.kik.datastation.dto.ds.async.Message;
import nl.kik.datastation.dto.vc.AbstractVerifiableCredentialTest;

@Slf4j
class VerifiableCredentialServiceTest extends AbstractVerifiableCredentialTest {
	private VerifiableCredentialService service;

	@BeforeEach
	void setUpService() throws Exception {
		service = new VerifiableCredentialService();
	}

	@Test
	void testSave() {
		messages.forEach(m -> {
			JOSEObject wrapped = service.wrap(m);
			System.out.println("Header: " + wrapped.getHeader().toJSONObject());
			System.out.println("Payload: " + wrapped.getPayload().toJSONObject());
		});
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
