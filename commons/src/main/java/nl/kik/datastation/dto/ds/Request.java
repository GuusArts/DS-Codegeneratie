package nl.kik.datastation.dto.ds;

import java.net.URL;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Request<T> extends Message<T> {
	private URL replyUrl;
}
