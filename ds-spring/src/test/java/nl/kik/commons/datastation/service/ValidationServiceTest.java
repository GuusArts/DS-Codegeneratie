package nl.kik.commons.datastation.service;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import foundation.identity.jsonld.JsonLDUtils;
import nl.kik.commons.datastation.dto.didcomm.Code;
import nl.kik.commons.datastation.dto.didcomm.Error;
import nl.kik.commons.datastation.dto.didcomm.ProblemReport;
import nl.kik.commons.datastation.dto.didcomm.Scope;
import nl.kik.commons.datastation.dto.didcomm.Sorter;
import nl.kik.commons.datastation.dto.ds.AskResult;
import nl.kik.commons.datastation.dto.ds.Binding;
import nl.kik.commons.datastation.dto.ds.Header;
import nl.kik.commons.datastation.dto.ds.RDFType;
import nl.kik.commons.datastation.dto.ds.SelectBody;
import nl.kik.commons.datastation.dto.ds.SelectResult;
import nl.kik.commons.datastation.dto.kikv.QueryRequest;
import nl.kik.commons.datastation.dto.kikv.QueryResponse;
import nl.kik.commons.datastation.dto.kikv.Request;
import nl.kik.commons.datastation.dto.kikv.Response;
import nl.kik.commons.datastation.dto.kikv.Result;
import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.dto.kikv.ValidatedQueryCredential;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
import nl.kik.commons.datastation.json.JWSSignedResultSet;

public class ValidationServiceTest {
	protected static final ZoneId ZONE = ZoneId.of("UTC");
	private ValidationService service;

	{
		JWSSignedResultSet.validator = new NoopCryptoService();
	}

	@BeforeEach
	void setUpService() throws Exception {
		service = new ValidationService(new NoopCryptoService(), false);
	}

	private ValidatedQueryCredential query() {
		return ValidatedQueryCredential.builder() //
				.id(URI.create("urn:whatever")) //
				.subjectId(URI.create("urn:from")) //
				.identifier("b7285a98-507c-49d0-9a96-3b76d197c99b") //
				.issuer(URI.create("did:example:76e12ec712ebc6f1c221ebfeb1f")) //
				.issuanceDate(JsonLDUtils.stringToDate("2019-06-16T18:56:59Z"))//
				.expirationDate(JsonLDUtils.stringToDate("2019-06-17T18:56:59Z")) //
				.name("2.3.1 Ziekteverzuimpercentage")//
				.description(
						"CBS/Vernet: Het ziekteverzuimpercentage is het totaal aantal ziektedagen van de personeelsleden, in procenten van het totaal aantal beschikbare (werk-/kalender) dagen van de werknemers in de verslagperiode. Het ziekteverzuimpercentage is inclusief het verzuim langer dan een jaar en exclusief zwangerschaps- en bevallingsverlof.")//
				.profile("https://kik-v.gitlab.io/uitwisselprofielen/uitwisselprofiel-odb/") //
				.ontology("https://raw.githubusercontent.com/kik-v/onto-kik/master/kik-v.owl") //
				.sparql("...") //
				.build();
	}

	private VerifiablePresentation vp() {
		return VerifiablePresentation.builder() //
				.holder(URI.create("urn:from")).verifiableCredential(query()) //
				.build();
	}

	private Request request() {
		return Request.builder() //
				.id(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
				.from(URI.create("urn:from")) //
				.singleTo(URI.create("urn:to")) //
				.created_time(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.expires_time(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.body(QueryRequest.builder() //
						.vp(vp()) //
						.param_values("params") //
						.build()) //
				.build();
	}

	private Response response() {
		return Response.builder() //
				.id(UUID.fromString("3fa682fa-8c98-4393-aef3-97b1a89da618")) //
				.thid(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
				.from(URI.create("urn:to")) //
				.singleTo(URI.create("urn:from")) //
				.created_time(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.expires_time(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.body(QueryResponse.builder() //
						.response(resultset()) //
						.build()) //
				.build();
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

	public nl.kik.commons.datastation.dto.didcomm.Error error() {
		return nl.kik.commons.datastation.dto.didcomm.Error.builder() //
				.id(UUID.fromString("c6d96d04-c988-41e2-a6e4-53d1a363659f")) //
				.pthid(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
				.from(URI.create("urn:to")) //
				.singleTo(URI.create("urn:from")) //
				.created_time(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.expires_time(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.singleAck(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
				.body(ProblemReport.builder() //
						.code(Code.builder() //
								.sorter(Sorter.Error) //
								.scope(Scope.Protocol) //
								.descriptor("req") //
								.descriptor("out-of-band") //
								.build()) //
						.arg("arg") //
						.comment("comment") //
						.escalate_to("escalate") //
						.build())//
				.build();
	}

	@Test
	void test() throws Exception {
		Request request = request();
		PlainJWT senderOauth = new PlainJWT(new JWTClaimsSet.Builder() //
				.issuer("urn:to") //
				.subject("urn:from") //
				.claim("service", "didcomm-service-kikv") //
				.build());
		service.validateFull(senderOauth, request);

		Response response = response();
		PlainJWT recipientOauth = new PlainJWT(new JWTClaimsSet.Builder() //
				.issuer("urn:from") //
				.subject("urn:to") //
				.claim("service", "didcomm-service-kikv") //
				.build());
		service.validateFull(recipientOauth, request, response);

		Error error = error();
		service.validateFull(recipientOauth, request, error);
	}

}
