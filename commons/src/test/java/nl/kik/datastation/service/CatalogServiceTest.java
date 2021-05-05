package nl.kik.datastation.service;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.kik.datastation.dto.dcat.AbstractDCATTest;
import nl.kik.datastation.dto.dcat.DataService;
import nl.kik.datastation.dto.dcat.Dataset;
import nl.kik.datastation.dto.dcat.kikv.Constants;

class CatalogServiceTest extends AbstractDCATTest {
	private CatalogService service;

	@BeforeEach
	void setUpService() throws Exception {
		service = new CatalogService();
	}

	@Test
	void testGetDatasets() {
		Collection<Dataset> datasets = service.getDatasets(catalog, Constants.STANDARD_VERIFIED_SPARQL);
		assertEquals(1, datasets.size());
		assertEquals(dataset, datasets.iterator().next());
	}

	@Test
	void testGetDatasetsNone() throws MalformedURLException {
		Collection<Dataset> datasets = service.getDatasets(catalog, new URL("http://example.org/non/existant"));
		assertTrue(datasets.isEmpty());
	}

	@Test
	void testGetEndpoints() {
		Collection<DataService> endpoints = service.getEndpoints(dataset, Constants.STANDARD_VERIFIED_SPARQL);
		assertEquals(1, endpoints.size());
		assertEquals(dataservice, endpoints.iterator().next());

		endpoints = service.getEndpoints(dataset, Constants.STANDARD_SPARQL);
		assertEquals(2, endpoints.size());
		assertEquals(Set.of(sparqlservice, sparqlservice2), new HashSet<>(endpoints));

		endpoints = service.getEndpoints(dataset, Constants.STANDARD_RDF);
		assertTrue(endpoints.isEmpty());
	}

	@Test
	void testGetEndpoint() {
		Optional<DataService> endpoints = service.getEndpoint(dataset, Constants.STANDARD_VERIFIED_SPARQL);
		assertTrue(endpoints.isPresent());
		assertEquals(dataservice, endpoints.get());

		endpoints = service.getEndpoint(dataset, Constants.STANDARD_SPARQL);
		assertTrue(endpoints.isPresent());
		assertTrue(Set.of(sparqlservice, sparqlservice2).contains(endpoints.get()));

		endpoints = service.getEndpoint(dataset, Constants.STANDARD_RDF);
		assertFalse(endpoints.isPresent());
	}
}
