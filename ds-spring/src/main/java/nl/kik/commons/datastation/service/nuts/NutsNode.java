package nl.kik.commons.datastation.service.nuts;

import java.util.List;

import javax.naming.directory.SearchResult;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;

import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.credential.PresentationVerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.SearchVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.VerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.crypto.SignJws;
import nl.kik.commons.datastation.dto.nuts.didman.CompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedCompoundService;
import nl.kik.commons.datastation.dto.nuts.didman.CreatedEndpoint;
import nl.kik.commons.datastation.dto.nuts.didman.Endpoint;
import nl.kik.commons.datastation.dto.nuts.didman.ServiceEndpoint;
import nl.kik.commons.datastation.dto.nuts.oauth.CreateJwtGrant;
import nl.kik.commons.datastation.dto.nuts.oauth.GrantedJwt;

public interface NutsNode extends RemoteNutsNode {
	@PostMapping("/internal/vcr/v2/issuer/vc")
	VerifiableCredential issueVC(@RequestBody CreateVerifiableCredential body);

	@PostMapping("/internal/vcr/v2/verifier/vc")
	VerificationResult verifyVC(@RequestBody VerifyVerifiableCredential body);

	@PostMapping("/internal/vcr/v2/search")
	SearchResult searchVC(@RequestBody SearchVerifiableCredential body);

	@PostMapping("/internal/vcr/v2/holder/vp")
	VerifiablePresentation issueVP(@RequestBody CreateVerifiablePresentation body);

	@PostMapping("/internal/vcr/v2/verifier/vp")
	PresentationVerificationResult verifyVP(@RequestBody VerifyVerifiablePresentation body);

	@PostMapping("/internal/crypto/v1/sign_jws")
	String signJws(@RequestBody SignJws<?> body);

	@PostMapping("/internal/auth/v1/jwt-grant")
	GrantedJwt createJwtGrant(@RequestBody CreateJwtGrant body);

	@RequestMapping(method = RequestMethod.HEAD, path = "/internal/auth/v1/accesstoken/verify")
	void verifyToken(@RequestHeader("Authorization") String token);

	@GetMapping("/internal/didman/v1/did/{did}/counpundservice/{compoundServiceType}/endpoint/{endpointType}")
	Endpoint retrieveEndpoint(@PathVariable String did, @PathVariable String compoundServiceType,
			@PathVariable String endpointType, @RequestParam(required = false) Boolean resolve);

	@PostMapping("/internal/didman/v1/did/{did}/endpoint")
	CreatedEndpoint addServiceEndpoint(@PathVariable String did, @RequestBody ServiceEndpoint endpoint);

	@DeleteMapping("/internal/didman/v1/did/{did}/endpoint/{type}")
	void deleteServiceEndpoint(@PathVariable String did, @PathVariable String type);

	@PostMapping("/internal/didman/v1/did/{did}/compountservice")
	CreatedCompoundService addCompoundService(@PathVariable String did, @RequestBody CompoundService service);

	@GetMapping("/internal/didman/v1/did/{did}/compoundservice")
	List<CreatedCompoundService> listCompountServices(@PathVariable String did);

	@DeleteMapping("/internal/didman/v1/service/{id}")
	void deleteService(@PathVariable String id);
}
