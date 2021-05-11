package nl.kik.datastation.mvc;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.ds.AskResult;
import nl.kik.datastation.dto.ds.Binding;
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

@RestController
@Slf4j
@SpringBootApplication(scanBasePackages = "nl.kik.datastation")
public class SerializationController {
	protected static final ZoneId ZONE = ZoneId.systemDefault();

	@GetMapping("/request")
	public Request<VerifiablePresentation> request() throws MalformedURLException {
		log.info("GET request");
		ValidatedQuery credential = ValidatedQuery.builder() //
				.keyId("urn:centralkey") //
				.from("did:central") //
				.to(Collections.singletonList("did:sender")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.profile("urn:profile") //
				.ontology("urn:ontology") //
				.query("SELECT ?s ?p ?o WHERE { ?s ?p ?o }") //
				.build();

		VerifiablePresentation presentation = VerifiablePresentation.builder() //
				.keyId("urn:userkey") //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.credential(credential) //
				.build();

		Request<VerifiablePresentation> message = Request.<VerifiablePresentation>builder() //
				.keyId("urn:userkey") //
				.body(presentation) //
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
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/error")
	public ReturnMessage<?> request(@RequestBody ErrorReport<String> error) {
		log.info("POST error {}", error);
		return error;
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

		Response<Result> message = Response.<Result>builder() //
				.keyId("urn:userkey") //
				.body(select) //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/select")
	public ReturnMessage<?> select(@RequestBody Response<Result> select) {
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

		Response<Result> message = Response.<Result>builder() //
				.keyId("urn:userkey") //
				.body(ask) //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		return message;
	}

	@PostMapping("/ask")
	public ReturnMessage<?> ask(@RequestBody Response<Result> ask) {
		log.info("POST ask {}", ask);
		return ask;
	}

	@GetMapping("/vp")
	public VerifiablePresentation vp() throws MalformedURLException {
		log.info("GET vp");

		ValidatedQuery credential = ValidatedQuery.builder() //
				.keyId("urn:centralkey") //
				.from("did:central") //
				.to(Collections.singletonList("did:sender")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.profile("urn:profile") //
				.ontology("urn:ontology") //
				.query("SELECT ?s ?p ?o WHERE { ?s ?p ?o }") //
				.build();

		VerifiablePresentation presentation = VerifiablePresentation.builder() //
				.keyId("urn:userkey") //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.credential(credential) //
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
				.from("did:central") //
				.to(Collections.singletonList("did:sender")) //
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
