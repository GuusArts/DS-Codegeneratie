package nl.kik.commons.datastation.service.nuts;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.credential.PresentationVerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.SearchResult;
import nl.kik.commons.datastation.dto.nuts.credential.SearchVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.VerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.crypto.SignJws;
import nl.kik.commons.datastation.dto.nuts.didman.CompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.ContactInformation;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedCompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedEndpoint;
import nl.kik.commons.datastation.dto.nuts.didman.Endpoint;
import nl.kik.commons.datastation.dto.nuts.didman.ServiceEndpoint;
import nl.kik.commons.datastation.dto.nuts.oauth.CreateJwtGrant;
import nl.kik.commons.datastation.dto.nuts.oauth.GrantedJwt;
import nl.kik.commons.datastation.dto.nuts.vdr.DIDResolutionResult;

@Slf4j
public class DefaultNutsClient implements NutsNode {
    private RestTemplate internalNutsRestClient;
    private String endpoint;

    public DefaultNutsClient(String endpoint, RestTemplate internalNutsRestClient) {
        this.endpoint = endpoint;
        this.internalNutsRestClient = internalNutsRestClient;
    }

    @Override
    public VerifiableCredential issueVC(CreateVerifiableCredential body) throws RestClientException, NutsException {
        return requireResponse(internalNutsRestClient.postForEntity(url("internal/vcr/v2/issuer/vc", Map.of()), body,
                VerifiableCredential.class));
    }

    @Override
    public VerificationResult verifyVC(VerifyVerifiableCredential body) {
        return requireResponse(internalNutsRestClient.postForEntity(url("internal/vcr/v2/verifier/vc", Map.of()), body,
                VerificationResult.class));
    }

    @Override
    public SearchResult searchVC(SearchVerifiableCredential body) {
        return requireResponse(internalNutsRestClient.postForEntity(url("internal/vcr/v2/search", Map.of()), body,
                SearchResult.class));
    }

    @Override
    public VerifiablePresentation issueVP(CreateVerifiablePresentation body) {
        return requireResponse(internalNutsRestClient.postForEntity(url("internal/vcr/v2/holder/vp", Map.of()), body,
                VerifiablePresentation.class));
    }

    @Override
    public PresentationVerificationResult verifyVP(VerifyVerifiablePresentation body) {
        return requireResponse(internalNutsRestClient.postForEntity(url("internal/vcr/v2/verifier/vp", Map.of()), body,
                PresentationVerificationResult.class));
    }

    @Override
    public DIDResolutionResult resolveDID(String did) {
        return requireResponse(internalNutsRestClient.getForEntity(url("internal/vdr/v1/did/{did}", //
                Map.of( //
                        "did", did //
                )), DIDResolutionResult.class));
    }

    @Override
    public String signJws(SignJws<?> body) {
        return requireResponse(
                internalNutsRestClient.postForEntity(url("internal/crypto/v1/sign_jws", Map.of()), body, String.class));
    }

    @Override
    public GrantedJwt createJwtGrant(CreateJwtGrant body) {
        return requireResponse(internalNutsRestClient.postForEntity(url("internal/auth/v1/jwt-grant", Map.of()), body,
                GrantedJwt.class));
    }

    @Override
    public void verifyToken(String token) {
        checkResponse(internalNutsRestClient.execute(url("internal/auth/v1/accesstoken/verify", Map.of()),
                HttpMethod.HEAD, request -> {
                }, internalNutsRestClient.responseEntityExtractor(null)));
    }

    @Override
    public ContactInformation getContactInfo(String did) {
        return requireResponse(internalNutsRestClient.getForEntity(url("internal/didman/v1/did/{did}/contactinfo", //
                Map.of( //
                        "did", did //
                )), ContactInformation.class));
    }

    @Override
    public Endpoint retrieveEndpoint(String did, String compoundServiceType, String endpointType, Boolean resolve) {
        return requireResponse(internalNutsRestClient.getForEntity(
                url("internal/didman/v1/did/{did}/compoundservice/{compoundServiceType}/endpoint/{endpointType}", //
                        b -> b.queryParamIfPresent("resolve", Optional.ofNullable(resolve)), Map.of( //
                                "did", did, //
                                "compoundServiceType", compoundServiceType, //
                                "endpointType", endpointType //
                        )), Endpoint.class));
    }

    @Override
    public CreatedEndpoint addServiceEndpoint(String did, ServiceEndpoint endpoint) {
        return requireResponse(internalNutsRestClient.postForEntity(url("internal/didman/v1/did/{did}/endpoint", Map.of( //
                "did", did //
        )), endpoint, CreatedEndpoint.class));
    }

    @Override
    public void deleteServiceEndpoint(String did, String type) {
        checkResponse(internalNutsRestClient.execute(url("internal/didman/v1/did/{did}/endpoint/{type}", Map.of( //
                "did", did, //
                "type", type //
        )), HttpMethod.DELETE, request -> {
        }, internalNutsRestClient.responseEntityExtractor(null)), HttpStatus.NO_CONTENT);
    }

    @Override
    public CreatedCompoundService addCompoundService(String did, CompoundService service) {
        return requireResponse(
                internalNutsRestClient.postForEntity(url("internal/didman/v1/did/{did}/compoundservice", Map.of( //
                        "did", did //
                )), service, CreatedCompoundService.class));
    }

    @Override
    public List<CreatedCompoundService> listCompountServices(String did) {
        return List.of(requireResponse(
                internalNutsRestClient.getForEntity(url("internal/didman/v1/did/{did}/compoundservice", Map.of( //
                        "did", did //
                )), CreatedCompoundService[].class)));
    }

    @Override
    public void deleteService(String id) {
        checkResponse(internalNutsRestClient.execute(url("internal/didman/v1/service/{id}", Map.of( //
                "id", id //
        )), HttpMethod.DELETE, request -> {
        }, internalNutsRestClient.responseEntityExtractor(null)));
    }

    private <T> Optional<T> checkResponse(ResponseEntity<T> entity) throws NutsException {
        return checkResponse(entity, HttpStatus.OK);
    }

    private <T> T requireResponse(ResponseEntity<T> entity) throws NutsException {
        return checkResponse(entity).orElseThrow(() -> new NutsException(-1, "Did not receive expected response"));
    }

    private <T> Optional<T> checkResponse(ResponseEntity<T> entity, HttpStatus... ok) throws NutsException {
        List<HttpStatus> oks = ok == null ? List.of() : List.of(ok);
        if (oks.contains(entity.getStatusCode())) {
            if (entity.hasBody()) {
                return Optional.of(entity.getBody());
            }
            return Optional.empty();
        }
        throw new NutsException(entity.getStatusCodeValue(), entity.getStatusCode().getReasonPhrase());
    }

    private URI url(String path, Map<String, String> pathParameters) {
        return url(path, null, pathParameters);
    }

    private URI url(String path, Function<UriComponentsBuilder, UriComponentsBuilder> extra,
            Map<String, String> pathParameters) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(endpoint) //
                .path(path);
        b = extra == null ? b : extra.apply(b);
        URI uri = b.build(pathParameters);
        log.info("url {}", uri);
        return uri;
    }

}
