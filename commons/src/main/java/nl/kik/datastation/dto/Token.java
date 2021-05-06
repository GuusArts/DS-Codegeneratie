package nl.kik.datastation.dto;

import java.time.ZonedDateTime;

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
	private String from, to;
	private ZonedDateTime expiration, creation;
}
