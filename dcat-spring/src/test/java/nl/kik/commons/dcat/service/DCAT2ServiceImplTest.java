package nl.kik.commons.dcat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.time.ZonedDateTime;

import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import nl.kik.commons.dcat.config.DCAT2AutoConfiguration;
import nl.kik.commons.dcat.model.Catalog;
import nl.kik.commons.dcat.model.Dataset;
import nl.kik.commons.dcat.model.Distribution;
import nl.kik.commons.dto.Graph;

/**
 * Tests for DCAT2ServiceImpl.
 */
@SpringBootTest(classes = DCAT2AutoConfiguration.class)
public class DCAT2ServiceImplTest {

    @Autowired
    private DCAT2Service dcat2Service;

    @Test
    public void testSaveAndLoadCatalog() {
        // Create a catalog
        Catalog catalog = Catalog.builder()
                .id("http://example.org/catalog/1")
                .title("Example Catalog")
                .description("An example catalog")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .build();

        // Create a dataset
        Dataset dataset = Dataset.builder()
                .id("http://example.org/dataset/1")
                .title("Example Dataset")
                .description("An example dataset")
                .build();

        // Create a distribution
        Distribution distribution = Distribution.builder()
                .id("http://example.org/distribution/1")
                .title("Example Distribution")
                .description("An example distribution")
                .accessURL(URI.create("http://example.org/dataset/1/access"))
                .downloadURL(URI.create("http://example.org/dataset/1/download"))
                .mediaType("application/json")
                .format("JSON")
                .build();

        // Add the distribution to the dataset
        dataset.addDistribution(distribution);

        // Add the dataset to the catalog
        catalog.addDataset(dataset);

        // Save the catalog to RDF
        Graph<org.apache.jena.rdf.model.Model> graph = Graph.create(ModelFactory.createDefaultModel());
        dcat2Service.save(graph, catalog);

        // Load the catalog from RDF
        Catalog loadedCatalog = dcat2Service.loadCatalog(graph, "http://example.org/catalog/1");

        // Verify the loaded catalog
        assertNotNull(loadedCatalog);
        assertEquals("Example Catalog", loadedCatalog.getTitle());
        assertEquals("An example catalog", loadedCatalog.getDescription());
        assertNotNull(loadedCatalog.getDataset());
        assertEquals(1, loadedCatalog.getDataset().size());

        // Verify the loaded dataset
        Dataset loadedDataset = loadedCatalog.getDataset().get(0);
        assertEquals("Example Dataset", loadedDataset.getTitle());
        assertEquals("An example dataset", loadedDataset.getDescription());
        assertNotNull(loadedDataset.getDistribution());
        assertEquals(1, loadedDataset.getDistribution().size());

        // Verify the loaded distribution
        Distribution loadedDistribution = loadedDataset.getDistribution().get(0);
        assertEquals("Example Distribution", loadedDistribution.getTitle());
        assertEquals("An example distribution", loadedDistribution.getDescription());
        assertEquals(URI.create("http://example.org/dataset/1/access"), loadedDistribution.getAccessURL());
        assertEquals(URI.create("http://example.org/dataset/1/download"), loadedDistribution.getDownloadURL());
        assertEquals("application/json", loadedDistribution.getMediaType());
        assertEquals("JSON", loadedDistribution.getFormat());
    }
}