package nl.kik.commons.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.nuts.NutsOrganizationCredential;
import nl.kik.commons.datastation.dto.nuts.credential.SearchOptions;
import nl.kik.commons.datastation.dto.nuts.credential.SearchResult;
import nl.kik.commons.datastation.dto.nuts.credential.SearchVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.didman.CompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.ContactInformation;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedCompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedEndpoint;
import nl.kik.commons.datastation.dto.nuts.didman.Endpoint;
import nl.kik.commons.datastation.dto.nuts.didman.ServiceEndpoint;
import nl.kik.commons.datastation.dto.nuts.vdr.DIDResolutionResult;
import nl.kik.commons.datastation.service.nuts.DefaultNutsClient;

@Slf4j
@SpringBootTest(classes = DefaultNutsClientTest.Context.class)
@Disabled
public class DefaultNutsClientTest {

    static {
        System.setProperty("javax.net.ssl.trustStore",
                "/Users/michael/java/kik-station/decentral/src/main/resources/simulatie.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "simulatie");
    }

    private static final String VDID = "did:nuts:HXWJzajdPSmCGk6vboiBM4wEhJKJmYSBrrakMsnVnyC6";
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
    void textContactInfo() {
        DIDResolutionResult resolveDID = client.resolveDID(DID);
        log.info("DID {}", resolveDID);
        log.info("{}", resolveDID.getDocument().toJson(true));
        List<String> controllers = resolveDID.getDocument().getControllers();
        log.info("Controllers {}", controllers);
        assertEquals(1, controllers.size());

        SearchResult result = client.searchVC(SearchVerifiableCredential.builder() //
                .query(VerifiableCredential.builder() //
                        .type("NutsOrganizationCredential") //
                        .credentialSubject(CredentialSubject.builder() //
                                .id(URI.create(DID)) //
                                .build()) //
                        .build())
                .searchOptions(SearchOptions.builder() //
                        .allowUntrustedIssuer(false) //
                        .build())
                .build());
        log.info("Search {}", result);
        assertEquals(1, result.getVerifiableCredentials().size());
        result.getVerifiableCredentials()
                .forEach(c -> log.info("Credential {}", c.getVerifiableCredential().toJson(true)));

        NutsOrganizationCredential organization = NutsOrganizationCredential
                .fromJsonLDObject(result.getVerifiableCredentials().iterator().next().getVerifiableCredential());
        log.info("Organisation {}, City {}", organization.getName(), organization.getCity());

        ContactInformation contact = client.getContactInfo(VDID);
        log.info("Contact {}", contact);
        assertThrows(HttpClientErrorException.NotFound.class, () -> client.getContactInfo(DID));
    }

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
