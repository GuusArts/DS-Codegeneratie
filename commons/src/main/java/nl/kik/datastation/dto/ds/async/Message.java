package nl.kik.datastation.dto.ds.async;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.datastation.dto.Token;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Message<T> extends Token {
	private String threadId;
	private T body;
}
