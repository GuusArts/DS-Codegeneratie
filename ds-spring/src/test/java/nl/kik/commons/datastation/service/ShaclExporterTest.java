package nl.kik.commons.datastation.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShaclExporterTest {
	ShaclExporter exporter;

	@BeforeEach
	void setUp() throws Exception {
		exporter = new ShaclExporter(ModelFactory.createDefaultModel());
	}

	@Test
	void test() {
		Shapes s = read("../../../../../validation.ttl");
		Model export = exporter.export(s);
//		RDFDataMgr.write(System.out, export, RDFFormat.TURTLE_FLAT);
		Shapes fixpoint = Shapes.parse(export);
//		assertTrue(DeepEquals.deepEquals(fixpoint, s));
	}

	private Shapes read(String string) {
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, ShaclExporterTest.class.getResourceAsStream(string), Lang.TURTLE);
//		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_FLAT);
		return Shapes.parse(model);
	}

}
