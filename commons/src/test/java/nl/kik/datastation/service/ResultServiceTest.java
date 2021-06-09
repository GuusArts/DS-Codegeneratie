package nl.kik.datastation.service;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.util.JSONObjectUtils;

import lombok.extern.slf4j.Slf4j;
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
	void testLoad() throws ParseException, MalformedURLException, JOSEException {
		for (final Result m : messages) {
			final Map<String, Object> wrapped = service.wrap(m);
			final String serialized = JSONObjectUtils.toJSONString(wrapped);
			final Result unwrapped = service.unwrap(JSONObjectUtils.parse(serialized));
			ResultServiceTest.log.trace("Comparing");
			ResultServiceTest.log.trace("{}", m);
			ResultServiceTest.log.trace("{}", unwrapped);
			Assertions.assertEquals(m, unwrapped);
		}
	}

	@Test
	void testSave() {
		messages.forEach(FunctionWrapper.wrapper(m -> {
			final Map<String, Object> wrapped = service.wrap(m);
			System.out.println(wrapped);
		}));
	}

}
