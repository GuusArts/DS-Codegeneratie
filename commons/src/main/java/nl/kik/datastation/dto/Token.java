package nl.kik.datastation.dto;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode
public abstract class Token {
	private String id;
	private String keyId;
	private String from;
	private List<String> to;
	private ZonedDateTime validFrom, expiration, creation;
}
