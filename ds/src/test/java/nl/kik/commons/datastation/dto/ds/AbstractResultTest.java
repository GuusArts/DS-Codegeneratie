package nl.kik.commons.datastation.dto.ds;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

import nl.kik.commons.datastation.dto.ds.AskResult;
import nl.kik.commons.datastation.dto.ds.Binding;
import nl.kik.commons.datastation.dto.ds.Header;
import nl.kik.commons.datastation.dto.ds.RDFType;
import nl.kik.commons.datastation.dto.ds.Result;
import nl.kik.commons.datastation.dto.ds.SelectBody;
import nl.kik.commons.datastation.dto.ds.SelectResult;

public abstract class AbstractResultTest {
	private AskResult ask;
	private SelectResult select;
	protected List<Result> messages;

	@BeforeEach
	void setUp() throws Exception {
		ask = AskResult.builder() //
				.head(Header.builder().build()) //
				.value(true) //
				.build();

		select = SelectResult.builder() //
				.head(Header.builder() //
						.link(List.of(new URL("http://example.com"))) //
						.vars(List.of("a", "b")) //
						.build()) //
				.results(SelectBody.builder() //
						.bindings(List.of( //
								Map.of("a", Binding.builder().value("1").type(RDFType.literal).build()), //
								Map.of("a", Binding.builder().value("http://example.com").type(RDFType.uri).build()), //
								Map.of("b",
										Binding.builder().value("hello").type(RDFType.literal).language("en").build()), //
								Map.of("a",
										Binding.builder().value("world").type(RDFType.literal).datatype("xsd:string")
												.build(), //
										"b", Binding.builder().value("b23").type(RDFType.bnode).build()) //
						)) //
						.build())//
				.build();

		messages = List.of(ask, select);
	}

}
