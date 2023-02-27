package nl.kik.commons.datastation.service.nuts;

import javax.naming.directory.SearchResult;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;

import nl.kik.commons.datastation.dto.nuts.CreateVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.CreateVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.PresentationVerificationResult;
import nl.kik.commons.datastation.dto.nuts.SearchVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.VerificationResult;
import nl.kik.commons.datastation.dto.nuts.VerifyVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.VerifyVerifiablePresentation;

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
}
