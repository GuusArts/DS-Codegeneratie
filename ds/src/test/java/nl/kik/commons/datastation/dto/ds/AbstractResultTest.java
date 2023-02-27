package nl.kik.commons.datastation.dto.ds;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractResultTest {
	private AskResult ask;
	private SelectResult select;
	protected List<SPARQLResult> messages;

	@BeforeEach
	void setUp() {
		ask = AskResult.builder() //
				.head(Header.builder().build()) //
				.value(true) //
				.build();

		select = SelectResult.builder() //
				.head(Header.builder() //
						.link(List.of(URI.create("http://example.com"))) //
						.vars(List.of("a", "b")) //
						.build()) //
				.results(SelectBody.builder() //
						.bindings(List.of( //
								Map.of("a", Binding.builder().value("1").type(RDFType.literal).build()), //
								Map.of("a", Binding.builder().value("http://example.com").type(RDFType.uri).build()), //
								Map.of("b", Binding.builder().value("hello").type(RDFType.literal).language("en").build()), //
								Map.of("a", Binding.builder().value("world").type(RDFType.literal).datatype("xsd:string").build(), //
										"b", Binding.builder().value("b23").type(RDFType.bnode).build()) //
						)) //
						.build())//
				.build();

		messages = List.of(ask, select);
	}

}
