package nl.kik.commons.datastation.mvc;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.danubetech.verifiablecredentials.VerifiablePresentation;

import foundation.identity.jsonld.JsonLDUtils;
import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.didcomm.Code;
import nl.kik.commons.datastation.dto.didcomm.Message;
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
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.credential.PresentationVerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.ProofPurpose;
import nl.kik.commons.datastation.dto.nuts.credential.Revocation;
import nl.kik.commons.datastation.dto.nuts.credential.SearchOptions;
import nl.kik.commons.datastation.dto.nuts.credential.SearchResult;
import nl.kik.commons.datastation.dto.nuts.credential.SearchVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.VerifiableCredentialSearchResult;
import nl.kik.commons.datastation.dto.nuts.credential.VerificationOptions;
import nl.kik.commons.datastation.dto.nuts.credential.VerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.credential.Visibility;
import nl.kik.commons.datastation.dto.nuts.crypto.SignResultSet;
import nl.kik.commons.datastation.dto.nuts.oauth.AccessToken;
import nl.kik.commons.datastation.dto.nuts.oauth.CreateAccessToken;
import nl.kik.commons.datastation.dto.nuts.oauth.CreateJwtGrant;
import nl.kik.commons.datastation.dto.nuts.oauth.GrantedJwt;

@RestController
@Slf4j
@SpringBootApplication(scanBasePackages = "nl.kik.commons.datastation")
public class SerializationController {
    protected static final ZoneId ZONE = ZoneId.of("UTC");

    @GetMapping("/kikv/query")
    public ValidatedQueryCredential query() {
        return ValidatedQueryCredential.builder() //
                .id(URI.create("did:nuts:HmTTxp5FxJLFtnfjxs1U9FUChrP5j7GpToivqdFt7Mnm")) //
                .queryId(URI.create("urn:uuid:b7285a98-507c-49d0-9a96-3b76d197c99b")) //
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

    @PostMapping("/kikv/query")
    public ValidatedQueryCredential query(@RequestBody ValidatedQueryCredential query) {
        return query;
    }

    @GetMapping("/nuts/createvc")
    public CreateVerifiableCredential createVC() {
        return CreateVerifiableCredential.builder(query()) //
                .visibility(Visibility.Private) //
                .publishToNetwork(true) //
                .build();
    }

    @PostMapping("/nuts/createvc")
    public CreateVerifiableCredential createVC(@RequestBody CreateVerifiableCredential create) {
        return create;
    }

    @GetMapping("/nuts/searchvc")
    public SearchVerifiableCredential searchVC() {
        return SearchVerifiableCredential.builder() //
                .query(query()) //
                .searchOptions(SearchOptions.builder() //
                        .allowUntrustedIssuer(true) //
                        .build()) //
                .build();
    }

    @PostMapping("/nuts/searchvc")
    public SearchVerifiableCredential searchVC(@RequestBody SearchVerifiableCredential create) {
        return create;
    }

    @GetMapping("/nuts/searchresult")
    public SearchResult searchResult() {
        return SearchResult.builder() //
                .verifiableCredential(VerifiableCredentialSearchResult.builder() //
                        .revocation(Revocation.builder() //
                                .date(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, SerializationController.ZONE)
                                        .toOffsetDateTime().toZonedDateTime()) //
                                .subject("subject") //
                                .reason("reason") //
                                .issuer(URI.create("urn:issuer")) //
                                .build()) //
                        .verifiableCredential(query()) //
                        .build()) //
                .verifiableCredential(VerifiableCredentialSearchResult.builder() //
                        .verifiableCredential(query()) //
                        .build()) //
                .build();
    }

    @PostMapping("/nuts/searchresult")
    public SearchResult searchResult(@RequestBody SearchResult create) {
        return create;
    }

    @GetMapping("/nuts/verifyvc")
    public VerifyVerifiableCredential verifyVC() {
        return VerifyVerifiableCredential.builder() //
                .verifiableCredential(query()) //
                .verificationOptions(VerificationOptions.builder() //
                        .allowUntrustedIssuer(true) //
                        .build()) //
                .build();
    }

    @PostMapping("/nuts/verifyvc")
    public VerifyVerifiableCredential verifyVC(@RequestBody VerifyVerifiableCredential create) {
        return create;
    }

    @GetMapping("/nuts/verificationresult")
    public VerificationResult verificationResult() {
        return VerificationResult.builder() //
                .validity(true) //
                .message("message") //
                .build();
    }

    @PostMapping("/nuts/verificationresult")
    public VerificationResult verificationResult(@RequestBody VerificationResult create) {
        return create;
    }

    @GetMapping("/nuts/createvp")
    public CreateVerifiablePresentation createVP() {
        return CreateVerifiablePresentation.builder() //
                .verifiableCredential(createVC()) //
                .verifiableCredential(createVC()) //
                .signerDID(URI.create("urn:signer")) //
                .challenge("challenge") //
                .domain("domain") //
                .expires(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
                .proofPurpose(ProofPurpose.assertionMethod) //
                .build();
    }

    @PostMapping("/nuts/createvp")
    public CreateVerifiablePresentation createVP(@RequestBody CreateVerifiablePresentation create) {
        return create;
    }

    @GetMapping("/nuts/verifyvp")
    public VerifyVerifiablePresentation verifyVP() {
        return VerifyVerifiablePresentation.builder() //
                .verifiablePresentation(vp()) //
                .validAt(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
                .verifyCredentials(true) //
                .build();
    }

    /**
     * @return
     */
    private VerifiablePresentation vp() {
        return VerifiablePresentation.builder() //
                .verifiableCredential(query()) //
                .build();
    }

    @PostMapping("/nuts/verifyvp")
    public VerifyVerifiablePresentation verifyVC(@RequestBody VerifyVerifiablePresentation create) {
        return create;
    }

    @GetMapping("/nuts/presentationverificationresult")
    public PresentationVerificationResult presentationVerificationResult() {
        return PresentationVerificationResult.builder() //
                .validity(true) //
                .message("message") //
                .credential(query()) //
                .build();
    }

    @PostMapping("/nuts/presentationverificationresult")
    public PresentationVerificationResult presentationVerificationResult(
            @RequestBody PresentationVerificationResult create) {
        return create;
    }

    @GetMapping("/nuts/signresultset")
    public SignResultSet signResultSet() {
        return SignResultSet.builder() //
                .header("header", "headerValue") //
                .detached(false) //
                .kid(URI.create("urn:from")) //
                .payload(resultset()) //
                .build();
    }

    @PostMapping("/nuts/signresultset")
    public SignResultSet signResultSet(@RequestBody SignResultSet create) {
        return create;
    }

    @GetMapping("/nuts/createjwt")
    public CreateJwtGrant createJwt() {
        return CreateJwtGrant.builder() //
                .authorizer(URI.create("urn:authorizer")) //
                .requester(URI.create("urn:requester")) //
                .identity(vp()) //
                .credentials(List.of(query())) //
                .service("service") //
                .build();
    }

    @PostMapping("/nuts/createjwt")
    public CreateJwtGrant createJwt(@RequestBody CreateJwtGrant create) {
        return create;
    }

    @GetMapping("/nuts/grantedjwt")
    public GrantedJwt grantedJwt() {
        return GrantedJwt.builder() //
                .bearer_token("token") //
                .authorization_server_endpoint("endpoint") //
                .build();
    }

    @PostMapping("/nuts/grantedjwt")
    public GrantedJwt grantedJwt(@RequestBody GrantedJwt create) {
        return create;
    }

    @GetMapping("/nuts/createtoken")
    public CreateAccessToken createToken() {
        return CreateAccessToken.builder() //
                .assertion("jwt token") //
                .build();
    }

    @PostMapping("/nuts/createtoken")
    public CreateAccessToken createToken(@RequestBody CreateAccessToken create) {
        return create;
    }

    @GetMapping("/nuts/token")
    public AccessToken token() {
        return AccessToken.builder() //
                .access_token("token") //
                .expires_in(600) //
                .token_type("token type") //
                .build();
    }

    @PostMapping("/nuts/token")
    public AccessToken token(@RequestBody AccessToken create) {
        return create;
    }

    @GetMapping("/didcomm/request")
    public Request request() {
        return Request.builder() //
                .id(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
                .from(URI.create("urn:from")) //
                .singleTo(URI.create("urn:to")) //
                .created_time(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
                .expires_time(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
                .body(QueryRequest.builder() //
                        .vp(vp()) //
                        .param_values("params") //
                        .build()) //
                .build();
    }

    @PostMapping("/didcomm/request")
    public Request request(@RequestBody Request create) {
        return create;
    }

    @PostMapping("/didcomm/pass")
    public <T> Message<T> pass(@RequestBody Message<T> create) {
        log.info("message class: {}", create.getClass().getSimpleName());
        log.info("body class: {}", create.getBody().getClass().getSimpleName());
        return create;
    }

    @GetMapping("/didcomm/error")
    public nl.kik.commons.datastation.dto.didcomm.Error error() {
        return nl.kik.commons.datastation.dto.didcomm.Error.builder() //
                .id(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
                .pthid(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
                .from(URI.create("urn:from")) //
                .singleTo(URI.create("urn:to")) //
                .created_time(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
                .expires_time(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
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

    @PostMapping("/didcomm/error")
    public nl.kik.commons.datastation.dto.didcomm.Error error(
            @RequestBody nl.kik.commons.datastation.dto.didcomm.Error create) {
        return create;
    }

    @GetMapping("/didcomm/response")
    public Response response() {
        return Response.builder() //
                .id(UUID.fromString("3fa682fa-8c98-4393-aef3-97b1a89da618")) //
                .thid(UUID.fromString("547429be-0b8c-4eb0-966d-6e1ab858127c")) //
                .from(URI.create("urn:to")) //
                .singleTo(URI.create("urn:from")) //
                .created_time(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
                .expires_time(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
                        .toZonedDateTime()) //
                .body(QueryResponse.builder() //
                        .response(resultset()) //
                        .build()) //
                .build();
    }

    /**
     * @return
     */
    private ResultSet resultset() {
        return ResultSet.builder() //
                .result(Result.builder() //
                        .messageId("547429be-0b8c-4eb0-966d-6e1ab858127c") //
                        .queryId("014b0186-a472-4c1f-8883-5d394dba7ee8") //
                        .result(ask()) //
                        .build()) //
                .result(Result.builder() //
                        .messageId("547429be-0b8c-4eb0-966d-6e1ab858127c") //
                        .queryId("afd5a04d-4747-44d6-bc59-43f5b6ecf8b5") //
                        .result(select()) //
                        .build()) //
                .build();
    }

    /**
     * @return
     * 
     */
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

    @PostMapping("/didcomm/response")
    public Response request(@RequestBody Response create) {
        return create;
    }

//	@GetMapping("/construct")
//	public ReturnMessage<?> construct() throws MalformedURLException, ParseException {
//		SerializationController.log.info("GET construct");
//
//		final Model model = ModelFactory.createDefaultModel();
//		model.add(model.createResource(), model.createProperty("http://example.com/property"), "world");
//		model.add(model.createResource("http://example.com/hello"), model.createProperty("http://example.com/property"),
//				model.createResource("http://example.com/world"));
//		model.add(model.createResource("http://example.com/girls"), model.createProperty("http://example.com/rule"),
//				model.createResource("http://example.com/world"));
//		model.add(model.createResource("http://example.com/girls"), model.createProperty("http://example.com/just_wanna"),
//				model.createResource("http://example.com/have_fun"));
//
//		final ConstructResult construct = sparql.wrap(model);
//
//		final Response<Map<String, Result>> message = Response.<Map<String, Result>>builder() //
//				.keyId("urn:userkey") //
//				.body(Map.of("urn:construct", construct)) //
//				.issuer("did:sender") //
//				.from("did:sender") //
//				.to(Collections.singletonList("did:recipient")) //
//				.expiration(ZonedDateTime.of(2030, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
//						.toZonedDateTime()) //
//				.validFrom(ZonedDateTime.of(2020, 1, 25, 0, 0, 0, 0, SerializationController.ZONE).toOffsetDateTime()
//						.toZonedDateTime()) //
//				.build();
//
//		return message;
//	}
//
//	@PostMapping("/construct")
//	public ReturnMessage<?> construct(@RequestBody final ReturnMessage<?> construct) {
//		SerializationController.log.info("POST construct {}", construct);
//		return construct;
//	}
//

}
