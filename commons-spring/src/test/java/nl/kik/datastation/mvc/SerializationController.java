package nl.kik.datastation.mvc;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.ds.AskResult;
import nl.kik.datastation.dto.ds.Binding;
import nl.kik.datastation.dto.ds.ConstructResult;
import nl.kik.datastation.dto.ds.Header;
import nl.kik.datastation.dto.ds.RDFType;
import nl.kik.datastation.dto.ds.Result;
import nl.kik.datastation.dto.ds.SelectBody;
import nl.kik.datastation.dto.ds.SelectResult;
import nl.kik.datastation.dto.ds.async.ErrorReport;
import nl.kik.datastation.dto.ds.async.Request;
import nl.kik.datastation.dto.ds.async.Response;
import nl.kik.datastation.dto.ds.async.ReturnMessage;
import nl.kik.datastation.dto.vc.ValidatedQuery;
import nl.kik.datastation.dto.vc.VerifiableCredential;
import nl.kik.datastation.dto.vc.VerifiablePresentation;
import nl.kik.datastation.service.SPARQLService;

@RestController
@Slf4j
@SpringBootApplication(scanBasePackages = "nl.kik.datastation")
public class SerializationController {
	protected static final ZoneId ZONE = ZoneId.systemDefault();

	@Autowired
	private SPARQLService sparql;

	@GetMapping("/request")
	public Request<VerifiablePresentation> request() throws MalformedURLException {
		log.info("GET request");
		ValidatedQuery credential = ValidatedQuery.builder() //
				.keyId("urn:centralkey") //
				.issuer("did:central") //
				.audience(Collections.singletonList("did:sender")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.profile("urn:profile") //
				.ontology("urn:ontology") //
				.query("SELECT ?s ?p ?o WHERE { ?s ?p ?o }") //
				.build();

		VerifiablePresentation presentation = VerifiablePresentation.builder() //
				.keyId("urn:userkey") //
				.issuer("did:sender") //
				.audience(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.credential(Collections.singletonList(credential)) //
				.build();

		Request<VerifiablePresentation> message = Request.<VerifiablePresentation>builder() //
				.keyId("urn:userkey") //
				.body(presentation) //
				.issuer("did:sender") //
				.from("did:sender") //
				.replyUrl(new URL("http://example.com/datastation")) //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/request")
	public Request<VerifiablePresentation> request(@RequestBody Request<VerifiablePresentation> request) {
		log.info("POST request {}", request);
		return request;
	}

	@GetMapping("/error")
	public ReturnMessage<?> error() {
		log.info("GET error");

		ErrorReport<String> message = ErrorReport.<String>builder() //
				.keyId("urn:userkey") //
				.body("Something went wrong") //
				.issuer("did:sender") //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/error")
	public ReturnMessage<?> request(@RequestBody ReturnMessage<?> error) {
		log.info("POST error {}", error);
		return error;
	}

	@GetMapping("/construct")
	public ReturnMessage<?> construct() throws MalformedURLException, ParseException {
		log.info("GET construct");

		Model model = ModelFactory.createDefaultModel();
		model.add(model.createResource(), model.createProperty("http://example.com/property"), "world");
		model.add(model.createResource("http://example.com/hello"), model.createProperty("http://example.com/property"),
				model.createResource("http://example.com/world"));
		model.add(model.createResource("http://example.com/girls"), model.createProperty("http://example.com/rule"),
				model.createResource("http://example.com/world"));
		model.add(model.createResource("http://example.com/girls"),
				model.createProperty("http://example.com/just_wanna"),
				model.createResource("http://example.com/have_fun"));

		ConstructResult construct = sparql.wrap(model);

		Response<Map<String, Result>> message = Response.<Map<String, Result>>builder() //
				.keyId("urn:userkey") //
				.body(Map.of("urn:construct", construct)) //
				.issuer("did:sender") //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/construct")
	public ReturnMessage<?> construct(@RequestBody ReturnMessage<?> construct) {
		log.info("POST construct {}", construct);
		return construct;
	}

	@GetMapping("/select")
	public ReturnMessage<?> select() throws MalformedURLException {
		log.info("GET select");

		SelectResult select = SelectResult.builder() //
				.head(Header.builder() //
						.link(List.of(new URL("http://example.com"))) //
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

		Response<Map<String, Result>> message = Response.<Map<String, Result>>builder() //
				.keyId("urn:userkey") //
				.body(Map.of("urn:select", select)) //
				.issuer("did:sender") //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/select")
	public ReturnMessage<?> select(@RequestBody ReturnMessage<?> select) {
		log.info("POST select {}", select);
		return select;
	}

	@GetMapping("/ask")
	public ReturnMessage<?> ask() throws MalformedURLException {
		log.info("GET ask");

		AskResult ask = AskResult.builder() //
				.head(Header.builder().build()) //
				.value(true) //
				.build();

		Response<Map<String, Result>> message = Response.<Map<String, Result>>builder() //
				.keyId("urn:userkey") //
				.body(Map.of("urn:ask", ask)) //
				.issuer("did:sender") //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/ask")
	public ReturnMessage<?> ask(@RequestBody ReturnMessage<?> ask) {
		log.info("POST ask {}", ask);
		return ask;
	}

	@PostMapping("/response")
	public ReturnMessage<?> response(@RequestBody ReturnMessage<?> response) {
		log.info("POST response {}", response);
		return response;
	}

	@GetMapping("/vp")
	public VerifiablePresentation vp() throws MalformedURLException {
		log.info("GET vp");

		ValidatedQuery credential = ValidatedQuery.builder() //
				.keyId("urn:centralkey") //
				.issuer("did:central") //
				.audience(Collections.singletonList("did:sender")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.profile("urn:profile") //
				.ontology("urn:ontology") //
				.query("SELECT ?s ?p ?o WHERE { ?s ?p ?o }") //
				.build();

		VerifiablePresentation presentation = VerifiablePresentation.builder() //
				.keyId("urn:userkey") //
				.issuer("did:sender") //
				.audience(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.credential(Collections.singletonList(credential)) //
				.build();

		return presentation;
	}

	@PostMapping("/vp")
	public VerifiablePresentation vp(@RequestBody VerifiablePresentation vp) {
		log.info("POST vp {}", vp);
		return vp;
	}

	@GetMapping("/vc")
	public VerifiableCredential vc() throws MalformedURLException {
		log.info("GET vc");

		ValidatedQuery credential = ValidatedQuery.builder() //
				.keyId("urn:centralkey") //
				.issuer("did:central") //
				.audience(Collections.singletonList("did:sender")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.profile("urn:profile") //
				.ontology("urn:ontology") //
				.query("SELECT ?s ?p ?o WHERE { ?s ?p ?o }") //
				.build();

		return credential;
	}

	@PostMapping("/vc")
	public VerifiableCredential vp(@RequestBody VerifiableCredential vc) {
		log.info("POST vc {}", vc);
		return vc;
	}

}
