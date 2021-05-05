package nl.kik.datastation.service;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.AbstractDSTest;

class MessageServiceTest extends AbstractDSTest {
	private MessageService service;

	@BeforeEach
	void setUpService() throws Exception {
		service = new MessageService();
	}

	@Test
	void test() {
		messages.forEach(
				m -> System.out.println(service.serialize(service.wrap(m, s -> new JSONObject(Map.of("plain", s))))));
	}

}
