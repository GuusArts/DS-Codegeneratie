package nl.kik.commons.datastation.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.Shapes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShaclExporterTest {
	ShaclExporter exporter;

	private Shapes read(final String string) {
		final Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, ShaclExporterTest.class.getResourceAsStream(string), Lang.TURTLE);
//		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_FLAT);
		return Shapes.parse(model);
	}

	@BeforeEach
	void setUp() throws Exception {
		exporter = new ShaclExporter(ModelFactory.createDefaultModel());
	}

	@Test
	void test1() {
		final Shapes s = read("../../../../../validation.ttl");
		final Model export = exporter.export(s);
//		RDFDataMgr.write(System.out, export, RDFFormat.TURTLE_FLAT);
		final Shapes fixpoint = Shapes.parse(export);
		Assertions.assertTrue(new ShapesComparator().test(s, fixpoint));
	}

	@Test
	void test2() {
		final Shapes s = read("../../../../../vph-validation.ttl");
		final Model export = exporter.export(s);
//		RDFDataMgr.write(System.out, export, RDFFormat.TURTLE_FLAT);
		final Shapes fixpoint = Shapes.parse(export);
		Assertions.assertTrue(new ShapesComparator().test(s, fixpoint));
	}

}
