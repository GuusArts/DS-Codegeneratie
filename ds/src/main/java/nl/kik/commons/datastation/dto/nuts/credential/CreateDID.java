package nl.kik.commons.datastation.dto.nuts.credential;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDID {
	@Singular
	private List<String> controllers;
	private Boolean assertionMethod;
	private Boolean authentication;
	private Boolean capabilityInvocation;
	private Boolean capabilityDelegation;
	private Boolean keyAgreement;
	private Boolean selfControl;
}
