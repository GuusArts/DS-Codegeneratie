package nl.kik.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.util.JSONObjectUtils;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import nl.kik.datastation.dto.ds.AbstractResultTest;
import nl.kik.datastation.dto.ds.Result;
import nl.kik.datastation.util.FunctionWrapper;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class ResultServiceTest extends AbstractResultTest {
	private ResultService service;

	@BeforeEach
	void setUpService() throws Exception {
		service = new ResultService();
	}

	@Test
	void testSave() {
		messages.forEach(FunctionWrapper.wrapper(m -> {
			JSONObject wrapped = service.wrap(m);
			System.out.println(wrapped);
		}));
	}

	@Test
	void testLoad() throws ParseException, MalformedURLException, JOSEException {
		for (Result m : messages) {
			JSONObject wrapped = service.wrap(m);
			String serialized = wrapped.toString();
			Result unwrapped = service.unwrap(JSONObjectUtils.parse(serialized));
			log.trace("Comparing");
			log.trace("{}", m);
			log.trace("{}", unwrapped);
			assertEquals(m, unwrapped);
		}
	}

}
