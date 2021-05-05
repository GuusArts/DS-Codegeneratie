package nl.kik.datastation.dto.ds;

import java.time.OffsetDateTime;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode
public class Message<T> {
	private String id, threadId;
	private String from, to;
	private OffsetDateTime expiration, creation;
	private T body;
}
