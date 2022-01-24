package nl.kik.commons.datastation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class VCTest {
//	@Test
	void test() {
		final Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, "https://www.w3.org/2018/credentials/v1", Lang.JSONLD);
		RDFDataMgr.write(System.out, model, RDFFormat.TTL);

	}
}
