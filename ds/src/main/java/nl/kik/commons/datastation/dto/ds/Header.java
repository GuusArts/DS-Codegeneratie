package nl.kik.commons.datastation.dto.ds;

import java.net.URL;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode
public class Header {
	private List<String> vars;
	private List<URL> link;
}
