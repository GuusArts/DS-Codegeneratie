package nl.kik.commons.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.nuts.didman.CompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedCompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedEndpoint;
import nl.kik.commons.datastation.dto.nuts.didman.Endpoint;
import nl.kik.commons.datastation.dto.nuts.didman.ServiceEndpoint;
import nl.kik.commons.datastation.service.nuts.DefaultNutsClient;

@Slf4j
@SpringBootTest(classes = DefaultNutsClientTest.Context.class)
public class DefaultNutsClientTest {

	static {
		System.setProperty("javax.net.ssl.trustStore",
				"/Users/michael/java/kik-station/decentral/src/main/resources/simulatie.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "simulatie");
	}

	private static final String DID = "did:nuts:AoQrzMTiLLfCqmK8CK1nkKShPdi4QUv2869oJzT2nAFt";
	private static final String ENDPOINT = "http://localhost:8080/";

	@SpringBootApplication(scanBasePackages = "nl.kik.commons.datastation")
	public static class Context {
		@Bean
		RestTemplate restTemplate(ObjectMapper objectMapper) {
			RestTemplate result = new RestTemplate();
			result.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
			result.getMessageConverters().removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
			result.getMessageConverters().add(new MappingJackson2HttpMessageConverter(objectMapper));
			return result;
		}

		@Bean
		DefaultNutsClient client(RestTemplate rest) {
			return new DefaultNutsClient("https://nuts-internal.acceptance.zin.ocs.nu", rest);
		}
	}

	@Autowired
	private DefaultNutsClient client;

	@Test
	void testEdnpoints() {
		assertThrows(HttpClientErrorException.NotAcceptable.class,
				() -> client.retrieveEndpoint(DID, "hello", "world", null));
		List<CreatedCompoundService> list = client.listCompountServices(DID);
		log.info("List<CreatedCompoundService> {}", list);
		assertEquals(0, list.size());
		try {
			CreatedEndpoint endpoint = client.addServiceEndpoint(DID, ServiceEndpoint.builder() //
					.type("test") //
					.endpoint(ENDPOINT) //
					.build());
			log.info("CreatedEndpoint {}", endpoint);
			try {
				CreatedCompoundService compound = client.addCompoundService(DID, CompoundService.builder() //
						.type("hello") //
						.serviceEndpoint(Map.of("world", DID + "/serviceEndpoint?type=test")) //
						.build());
				log.info("CreatedCompoundService {}", compound);
				list = client.listCompountServices(DID);
				log.info("List<CreatedCompoundService> {}", list);
				Endpoint resolved = client.retrieveEndpoint(DID, "hello", "world", null);
				log.info("Endpoint {}", resolved);
				assertEquals(ENDPOINT, resolved.getEndpoint());
				resolved = client.retrieveEndpoint(DID, "hello", "world", true);
				log.info("Endpoint {}", resolved);
				assertEquals(ENDPOINT, resolved.getEndpoint());
				resolved = client.retrieveEndpoint(DID, "hello", "world", false);
				log.info("Endpoint {}", resolved);
				assertNotEquals(ENDPOINT, resolved.getEndpoint());
			} finally {
				client.deleteServiceEndpoint(DID, "hello");
			}
		} finally {
			client.deleteServiceEndpoint(DID, "test");
		}
		assertThrows(HttpClientErrorException.NotAcceptable.class,
				() -> client.retrieveEndpoint(DID, "hello", "world", null));
	}
}
