package nl.kik.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.Graph;
import nl.kik.datastation.dto.RDFObject;
import nl.kik.datastation.dto.dcat.AbstractDCATTest;
import nl.kik.datastation.dto.dcat.Catalog;

@Slf4j
public class DCATServiceTest extends AbstractDCATTest {
	private DCATService service;

	@BeforeEach
	void setUpService() throws Exception {
		service = new DCATService();
	}

	@Test
	void testSave() {
		Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (RDFObject m : model) {
			service.save(g, m);
		}
		RDFService.snapshot(g, true, null);
	}

	@Test
	void testLoad() {
		Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (RDFObject m : model) {
			service.save(g, m);
		}
		for (RDFObject m : model) {
			Optional<RDFObject> o = service.lookupById(g, m.getId());
			if (o.isEmpty()) {
				fail("Not found " + m);
			} else {
				log.trace("Comparing");
				log.trace("{}", m);
				log.trace("{}", o.get());
				assertEquals(m, o.get());
			}
		}
	}
	
	@Test
	void testGetAllCatalogs() {
		Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (RDFObject m : model) {
			service.save(g, m);
		}
		Collection<? extends Catalog> catalogs = service.getAllCatalogs(g);
		log.trace("Found {} catalogs: {}", catalogs.size(), catalogs);
		assertEquals(1, catalogs.size());
		assertEquals(catalog, catalogs.iterator().next());
	}
}
