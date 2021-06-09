package nl.kik.commons.datastation.service;

import java.util.Collection;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.Graph;
import nl.kik.commons.datastation.dto.RDFObject;
import nl.kik.commons.datastation.dto.dcat.AbstractDCATTest;
import nl.kik.commons.datastation.dto.dcat.Catalog;
import nl.kik.commons.datastation.service.DCATService;
import nl.kik.commons.datastation.service.RDFService;

@Slf4j
public class DCATServiceTest extends AbstractDCATTest {
	private DCATService service;

	@BeforeEach
	void setUpService() throws Exception {
		service = new DCATService();
	}

	@Test
	void testGetAllCatalogs() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (final RDFObject m : model) {
			service.save(g, m);
		}
		final Collection<? extends Catalog> catalogs = service.getAllCatalogs(g);
		DCATServiceTest.log.trace("Found {} catalogs: {}", catalogs.size(), catalogs);
		Assertions.assertEquals(1, catalogs.size());
		Assertions.assertEquals(catalog, catalogs.iterator().next());
	}

	@Test
	void testLoad() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (final RDFObject m : model) {
			service.save(g, m);
		}
		for (final RDFObject m : model) {
			final Optional<RDFObject> o = service.lookupById(g, m.getId());
			if (o.isEmpty()) {
				Assertions.fail("Not found " + m);
			} else {
				DCATServiceTest.log.trace("Comparing");
				DCATServiceTest.log.trace("{}", m);
				DCATServiceTest.log.trace("{}", o.get());
				Assertions.assertEquals(m, o.get());
			}
		}
	}

	@Test
	void testSave() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (final RDFObject m : model) {
			service.save(g, m);
		}
		RDFService.snapshot(g, true, null);
	}
}
