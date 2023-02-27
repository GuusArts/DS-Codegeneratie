package nl.kik.commons.datastation.service.nuts;

import javax.naming.directory.SearchResult;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

public interface NutsNode {
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
}
