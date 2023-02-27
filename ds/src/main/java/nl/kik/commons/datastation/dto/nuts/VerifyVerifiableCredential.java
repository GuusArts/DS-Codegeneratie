package nl.kik.commons.datastation.dto.nuts;

import java.time.ZonedDateTime;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyVerifiableCredential {
	private VerifiableCredential verifiableCredential;
	private VerificationOptions verificationOptions;
}
