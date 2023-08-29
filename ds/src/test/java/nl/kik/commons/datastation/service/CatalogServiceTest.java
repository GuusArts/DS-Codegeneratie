package nl.kik.commons.datastation.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.kik.commons.datastation.dto.dcat.AbstractDCATTest;
import nl.kik.commons.datastation.dto.dcat.DataService;
import nl.kik.commons.datastation.dto.dcat.Dataset;
import nl.kik.commons.datastation.dto.dcat.kikv.Constants;

class CatalogServiceTest extends AbstractDCATTest {
	private CatalogService service;

	@BeforeEach
	void setUpService() throws Exception {
		service = new CatalogService();
	}

//	@Test
//	void testGetDatasets() {
//		final Collection<Dataset> datasets = service.getDatasets(catalog, Constants.STANDARD_VERIFIED_SPARQL);
//		Assertions.assertEquals(1, datasets.size());
//		Assertions.assertEquals(dataset, datasets.iterator().next());
//	}

	@Test
	void testGetDatasetsNone() throws URISyntaxException {
		final Collection<Dataset> datasets = service.getDatasets(catalog, new URI("http://example.org/non/existant"));
		Assertions.assertTrue(datasets.isEmpty());
	}

}
