package nl.kik.commons.datastation.dto.ds.async;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.Token;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Message<T> extends Token {
	private String from;
	private List<String> to;
	private String threadId;
	private T body;
}
