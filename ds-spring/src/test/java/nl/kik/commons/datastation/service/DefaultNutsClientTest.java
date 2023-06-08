package nl.kik.commons.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.ds.AskResult;
import nl.kik.commons.datastation.dto.ds.Binding;
import nl.kik.commons.datastation.dto.ds.Header;
import nl.kik.commons.datastation.dto.ds.RDFType;
import nl.kik.commons.datastation.dto.ds.SelectBody;
import nl.kik.commons.datastation.dto.ds.SelectResult;
import nl.kik.commons.datastation.dto.kikv.Result;
import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.dto.kikv.ValidatedQueryCredential;
import nl.kik.commons.datastation.dto.nuts.NutsOrganizationCredential;
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.credential.PresentationVerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.SearchOptions;
import nl.kik.commons.datastation.dto.nuts.credential.SearchResult;
import nl.kik.commons.datastation.dto.nuts.credential.SearchVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.credential.Visibility;
import nl.kik.commons.datastation.dto.nuts.crypto.SignResultSet;
import nl.kik.commons.datastation.dto.nuts.didman.CompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.ContactInformation;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedCompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedEndpoint;
import nl.kik.commons.datastation.dto.nuts.didman.Endpoint;
import nl.kik.commons.datastation.dto.nuts.didman.ServiceEndpoint;
import nl.kik.commons.datastation.dto.nuts.oauth.AccessToken;
import nl.kik.commons.datastation.dto.nuts.oauth.CreateJwtGrant;
import nl.kik.commons.datastation.dto.nuts.vdr.DIDResolutionResult;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
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
	private static final String ADID = "did:nuts:DZx5TChA4QmTF5iBtYGyTgcmyjuWqEjm7Zas9hXbSF7F";
	private static final String ENDPOINT = "http://localhost:8080/";
	private static final String NUTS = "https://nuts-internal.acceptance.zin.ocs.nu";
	private static final String NUTS_N2N = "https://nuts-n2n.acceptance.zin.ocs.nu";

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
			return new DefaultNutsClient(NUTS, rest);
		}
	}

	@Autowired
	private DefaultNutsClient client;

	@Test
	void testFindCredentials() {
		SearchResult result = client.searchVC(SearchVerifiableCredential.builder() //
				.query(VerifiableCredential.builder() //
						.type(NutsOrganizationCredential.TYPE) //
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
		result = client.searchVC(SearchVerifiableCredential.builder() //
				.query(ValidatedQueryCredential.builder() //
						.type(NutsOrganizationCredential.TYPE) //
						.credentialSubject(CredentialSubject.builder() //
								.id(URI.create(ADID)) //
								.build()) //
						.build())
				.searchOptions(SearchOptions.builder() //
						.allowUntrustedIssuer(false) //
						.build())
				.build());
		log.info("Search {}", result);
		assertFalse(result.getVerifiableCredentials().isEmpty());
		result.getVerifiableCredentials()
				.forEach(c -> log.info("Credential {}", c.getVerifiableCredential().toJson(true)));
	}

	@Test
	void testIssueAndValidateCredentials() {
		SearchResult result = client.searchVC(SearchVerifiableCredential.builder() //
				.query(VerifiableCredential.builder() //
						.type(NutsOrganizationCredential.TYPE) //
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

		VerifiableCredential vc = client.issueVC(CreateVerifiableCredential.from(ValidatedQueryCredential.builder() //
				.subjectId(URI.create(ADID)) //
				.issuer(URI.create(DID)) //
				.name("Test") //
				.description("Test description") //
				.identifier("id") //
				.expirationDate(Date.from(Instant.now().plus(365, ChronoUnit.DAYS))) //
				.ontology("urn:ontology") //
				.profile("urn:profile") //
				.sparql("SELECT ?s ?p ?o WHERE { ?s ?p ?o }") //
				.build()) //
				.visibility(Visibility.Public) //
				.publishToNetwork(true) //
				.build());
		log.info("VC {}", vc.toJson(true));

		VerifiablePresentation vp = client.issueVP(CreateVerifiablePresentation.builder() //
				.verifiableCredential(result.getVerifiableCredentials().iterator().next().getVerifiableCredential()) //
				.verifiableCredential(vc) //
				.signerDID(URI.create(DID)) //
				.build());
		log.info("VP {}", vp.toJson(true));

		PresentationVerificationResult validation = client.verifyVP(VerifyVerifiablePresentation.builder() //
				.validAt(ZonedDateTime.now()) //
				.verifiablePresentation(vp) //
				.verifyCredentials(true) //
				.build());
		log.info("Valdation result {}", validation);
		assertTrue(validation.isValidity());
	}

	@Test
	void testSign() throws JOSEException, ParseException {
		DIDResolutionResult resolveDID = client.resolveDID(DID);
		URI keyId = resolveDID.getDocument().getKeyAgreementVerificationMethods().stream() //
				.map(k -> k.getId()) //
				.findFirst().orElseThrow();

		JWSObject jws = client.signJws(SignResultSet.builder() //
				.detached(false) //
				.kid(keyId) //
				.payload(resultset()) //
				.build()); //
		log.info("Signed {}", jws);
		assertEquals("ES256", jws.getHeader().getAlgorithm().getName());

		Map<String, Object> jwkJson = resolveDID.getDocument().getKeyAgreementVerificationMethods().stream() //
				.filter(k -> k.getId().toString().equals(jws.getHeader().getKeyID())) //
				.map(k -> k.getPublicKeyJwk()) //
				.findFirst().orElseThrow();
		JWK jwk = JWK.parse(jwkJson);
		log.info("JWK {}", jwk);
		ECKey eckey = jwk.toECKey();
		log.info("ECKey {}", eckey);
		ECPublicKey key = eckey.toECPublicKey();
		log.info("Key {}", key);

		JWSVerifier verifier = new DefaultJWSVerifierFactory().createJWSVerifier(jws.getHeader(), key);
		boolean valid = jws.verify(verifier);
		log.info("Validates {}", valid);
		assertTrue(valid);
	}

	@Test
	void testOauth() throws ParseException {
		try {
			CreatedEndpoint endpoint = client.addServiceEndpoint(DID, ServiceEndpoint.builder() //
					.type("test") //
					.endpoint(NUTS_N2N + "/n2n/auth/v1/accesstoken") //
					.build());
			log.info("Created endpoint {}", endpoint);
			try {
				CreatedCompoundService compoundService = client.addCompoundService(DID, CompoundService.builder() //
						.type("hello") //
						.serviceEndpoint(Map.of("oauth", DID + "/serviceEndpoint?type=test")) //
						.build());
				log.info("Created compound service {}", compoundService);

				AccessToken accessToken = client.requestAccessToken(CreateJwtGrant.builder() //
						.authorizer(URI.create(DID)) //
						.requester(URI.create(ADID)) //
						.service("hello") //
						.build());
				log.info("Access token {}", accessToken);
				JWT token = JWTParser.parse(accessToken.getAccess_token());
				client.verifyToken(accessToken.getAccess_token());
				client.verifyToken(accessToken.getAccess_token()); // Test tokens can be re-used
				log.info("Header {}", token.getHeader());
				log.info("Body {}", token.getJWTClaimsSet());
				assertEquals(ADID, token.getJWTClaimsSet().getSubject());
				assertEquals(DID, token.getJWTClaimsSet().getIssuer());
				assertEquals("hello", token.getJWTClaimsSet().getStringClaim("service"));
				assertThrows(HttpClientErrorException.Forbidden.class, () -> client.verifyToken("fake"));
			} finally {
				client.deleteServiceEndpoint(DID, "hello");
			}
		} finally {
			client.deleteServiceEndpoint(DID, "test");
		}

	}

	@Test
	void testContactInfo() {
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
		result = client.searchVC(SearchVerifiableCredential.builder() //
				.query(NutsOrganizationCredential.builder() //
						.orgId(URI.create(DID)) //
						.build()) //
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
	void testEndpoints() {
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

	private ResultSet resultset() {
		return ResultSet.builder() //
				.result(Result.builder() //
						.messageId("547429be-0b8c-4eb0-966d-6e1ab858127c") //
						.queryId("b7285a98-507c-49d0-9a96-3b76d197c99b") //
						.result(ask()) //
						.build()) //
				.result(Result.builder() //
						.messageId("547429be-0b8c-4eb0-966d-6e1ab858127c") //
						.queryId("b7285a98-507c-49d0-9a96-3b76d197c99b") //
						.result(select()) //
						.build()) //
				.build();
	}

	private SelectResult select() {
		return SelectResult.builder() //
				.head(Header.builder() //
						.link(List.of(URI.create("http://example.com"))) //
						.vars(List.of("a", "b")) //
						.build()) //
				.results(SelectBody.builder() //
						.bindings(List.of( //
								Map.of("a", Binding.builder().value("1").type(RDFType.literal).build()), //
								Map.of("a", Binding.builder().value("http://example.com").type(RDFType.uri).build()), //
								Map.of("b",
										Binding.builder().value("hello").type(RDFType.literal).language("en").build()), //
								Map.of("a",
										Binding.builder().value("world").type(RDFType.literal).datatype("xsd:string")
												.build(), //
										"b", Binding.builder().value("b23").type(RDFType.bnode).build()) //
						)) //
						.build())//
				.build();
	}

	/**
	 * @return
	 */
	private AskResult ask() {
		return AskResult.builder() //
				.head(Header.builder().build()) //
				.value(true) //
				.build();
	}

}
