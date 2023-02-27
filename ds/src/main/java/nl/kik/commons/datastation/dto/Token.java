package nl.kik.commons.datastation.dto;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode
public abstract class Token {
	private String issuer;
	private List<String> audience;
	private String id;
	private String keyId;
	boolean publishedToNetwork;
	private ZonedDateTime issuanceDate, expirationDate;
}
