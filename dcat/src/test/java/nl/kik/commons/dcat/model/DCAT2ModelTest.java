package nl.kik.commons.dcat.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for the DCAT2 model classes.
 */
public class DCAT2ModelTest {

    @Test
    public void testCreateCatalog() {
        // Create a catalog
        Catalog catalog = Catalog.builder()
                .id("http://example.org/catalog/1")
                .title("Test Catalog")
                .description("A test catalog")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .language(URI.create("http://id.loc.gov/vocabulary/iso639-1/en"))
                .license(URI.create("http://creativecommons.org/licenses/by/4.0/"))
                .homepage(URI.create("http://example.org/catalog"))
                .build();
        
        // Add keywords and themes
        catalog.addKeyword("test");
        catalog.addKeyword("catalog");
        catalog.addTheme(URI.create("http://example.org/theme/test"));
        
        // Verify the catalog
        assertEquals("http://example.org/catalog/1", catalog.getId());
        assertEquals("Test Catalog", catalog.getTitle());
        assertEquals("A test catalog", catalog.getDescription());
        assertNotNull(catalog.getIssued());
        assertNotNull(catalog.getModified());
        assertEquals(URI.create("http://id.loc.gov/vocabulary/iso639-1/en"), catalog.getLanguage());
        assertEquals(URI.create("http://creativecommons.org/licenses/by/4.0/"), catalog.getLicense());
        assertEquals(URI.create("http://example.org/catalog"), catalog.getHomepage());
        assertEquals(2, catalog.getKeywords().size());
        assertEquals(1, catalog.getThemes().size());
    }
    
    @Test
    public void testCreateDataset() {
        // Create a dataset
        Dataset dataset = Dataset.builder()
                .id("http://example.org/dataset/1")
                .title("Test Dataset")
                .description("A test dataset")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .accrualPeriodicity(Duration.ofDays(7))
                .spatialCoverage(URI.create("http://example.org/spatial/1"))
                .spatialResolutionInMeters(10.0)
                .temporalResolution(Duration.ofHours(1))
                .build();
        
        // Add keywords and themes
        dataset.addKeyword("test");
        dataset.addKeyword("dataset");
        dataset.addTheme(URI.create("http://example.org/theme/test"));
        
        // Create and add a distribution
        Distribution distribution = Distribution.builder()
                .id("http://example.org/distribution/1")
                .title("Test Distribution")
                .description("A test distribution")
                .accessURL(URI.create("http://example.org/dataset/1/access"))
                .downloadURL(URI.create("http://example.org/dataset/1/download"))
                .mediaType("application/json")
                .format("JSON")
                .byteSize(1024L)
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .license(URI.create("http://creativecommons.org/licenses/by/4.0/"))
                .build();
        
        dataset.addDistribution(distribution);
        
        // Verify the dataset
        assertEquals("http://example.org/dataset/1", dataset.getId());
        assertEquals("Test Dataset", dataset.getTitle());
        assertEquals("A test dataset", dataset.getDescription());
        assertNotNull(dataset.getIssued());
        assertNotNull(dataset.getModified());
        assertEquals(Duration.ofDays(7), dataset.getAccrualPeriodicity());
        assertEquals(URI.create("http://example.org/spatial/1"), dataset.getSpatialCoverage());
        assertEquals(10.0, dataset.getSpatialResolutionInMeters());
        assertEquals(Duration.ofHours(1), dataset.getTemporalResolution());
        assertEquals(2, dataset.getKeywords().size());
        assertEquals(1, dataset.getThemes().size());
        assertEquals(1, dataset.getDistributions().size());
        
        // Verify the distribution
        Distribution retrievedDistribution = dataset.getDistributions().iterator().next();
        assertEquals("http://example.org/distribution/1", retrievedDistribution.getId());
        assertEquals("Test Distribution", retrievedDistribution.getTitle());
        assertEquals("A test distribution", retrievedDistribution.getDescription());
        assertEquals(URI.create("http://example.org/dataset/1/access"), retrievedDistribution.getAccessURL());
        assertEquals(URI.create("http://example.org/dataset/1/download"), retrievedDistribution.getDownloadURL());
        assertEquals("application/json", retrievedDistribution.getMediaType());
        assertEquals("JSON", retrievedDistribution.getFormat());
        assertEquals(1024L, retrievedDistribution.getByteSize());
        assertNotNull(retrievedDistribution.getIssued());
        assertNotNull(retrievedDistribution.getModified());
        assertEquals(URI.create("http://creativecommons.org/licenses/by/4.0/"), retrievedDistribution.getLicense());
    }
    
    @Test
    public void testCreateDataService() {
        // Create a data service
        DataService service = DataService.builder()
                .id("http://example.org/service/1")
                .title("Test Service")
                .description("A test service")
                .endpointURL(URI.create("http://example.org/service/1/endpoint"))
                .endpointDescription(URI.create("http://example.org/service/1/description"))
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .license(URI.create("http://creativecommons.org/licenses/by/4.0/"))
                .build();
        
        // Add keywords and themes
        service.addKeyword("test");
        service.addKeyword("service");
        service.addTheme(URI.create("http://example.org/theme/test"));
        
        // Create and add a dataset
        Dataset dataset = Dataset.builder()
                .id("http://example.org/dataset/1")
                .title("Test Dataset")
                .description("A test dataset")
                .build();
        
        service.addServesDataset(dataset);
        
        // Verify the data service
        assertEquals("http://example.org/service/1", service.getId());
        assertEquals("Test Service", service.getTitle());
        assertEquals("A test service", service.getDescription());
        assertEquals(URI.create("http://example.org/service/1/endpoint"), service.getEndpointURL());
        assertEquals(URI.create("http://example.org/service/1/description"), service.getEndpointDescription());
        assertNotNull(service.getIssued());
        assertNotNull(service.getModified());
        assertEquals(URI.create("http://creativecommons.org/licenses/by/4.0/"), service.getLicense());
        assertEquals(2, service.getKeywords().size());
        assertEquals(1, service.getThemes().size());
        assertEquals(1, service.getServesDatasets().size());
        
        // Verify the dataset
        Dataset retrievedDataset = service.getServesDatasets().iterator().next();
        assertEquals("http://example.org/dataset/1", retrievedDataset.getId());
        assertEquals("Test Dataset", retrievedDataset.getTitle());
        assertEquals("A test dataset", retrievedDataset.getDescription());
    }
    
    @Test
    public void testCreateCatalogRecord() {
        // Create a catalog record
        CatalogRecord record = CatalogRecord.builder()
                .id("http://example.org/record/1")
                .title("Test Record")
                .description("A test record")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .language(URI.create("http://id.loc.gov/vocabulary/iso639-1/en"))
                .status(URI.create("http://example.org/status/active"))
                .statusModified(ZonedDateTime.now())
                .publisher(URI.create("http://example.org/publisher/1"))
                .license(URI.create("http://creativecommons.org/licenses/by/4.0/"))
                .build();
        
        // Create and set a resource
        Dataset dataset = Dataset.builder()
                .id("http://example.org/dataset/1")
                .title("Test Dataset")
                .description("A test dataset")
                .build();
        
        record.setResource(dataset);
        
        // Verify the catalog record
        assertEquals("http://example.org/record/1", record.getId());
        assertEquals("Test Record", record.getTitle());
        assertEquals("A test record", record.getDescription());
        assertNotNull(record.getIssued());
        assertNotNull(record.getModified());
        assertEquals(URI.create("http://id.loc.gov/vocabulary/iso639-1/en"), record.getLanguage());
        assertEquals(URI.create("http://example.org/status/active"), record.getStatus());
        assertNotNull(record.getStatusModified());
        assertEquals(URI.create("http://example.org/publisher/1"), record.getPublisher());
        assertEquals(URI.create("http://creativecommons.org/licenses/by/4.0/"), record.getLicense());
        assertNotNull(record.getResource());
        
        // Verify the resource
        Dataset retrievedDataset = (Dataset) record.getResource();
        assertEquals("http://example.org/dataset/1", retrievedDataset.getId());
        assertEquals("Test Dataset", retrievedDataset.getTitle());
        assertEquals("A test dataset", retrievedDataset.getDescription());
    }
    
    @Test
    public void testCreateCompleteCatalog() {
        // Create a complete catalog with datasets, services, and records
        Catalog catalog = Catalog.builder()
                .id("http://example.org/catalog/1")
                .title("Complete Catalog")
                .description("A complete catalog with datasets, services, and records")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .language(URI.create("http://id.loc.gov/vocabulary/iso639-1/en"))
                .license(URI.create("http://creativecommons.org/licenses/by/4.0/"))
                .homepage(URI.create("http://example.org/catalog"))
                .build();
        
        // Create and add a dataset with a distribution
        Dataset dataset = Dataset.builder()
                .id("http://example.org/dataset/1")
                .title("Test Dataset")
                .description("A test dataset")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .build();
        
        Distribution distribution = Distribution.builder()
                .id("http://example.org/distribution/1")
                .title("Test Distribution")
                .description("A test distribution")
                .accessURL(URI.create("http://example.org/dataset/1/access"))
                .downloadURL(URI.create("http://example.org/dataset/1/download"))
                .mediaType("application/json")
                .format("JSON")
                .build();
        
        dataset.addDistribution(distribution);
        catalog.addDataset(dataset);
        
        // Create and add a data service
        DataService service = DataService.builder()
                .id("http://example.org/service/1")
                .title("Test Service")
                .description("A test service")
                .endpointURL(URI.create("http://example.org/service/1/endpoint"))
                .endpointDescription(URI.create("http://example.org/service/1/description"))
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .build();
        
        service.addServesDataset(dataset);
        catalog.addService(service);
        
        // Create and add a catalog record
        CatalogRecord record = CatalogRecord.builder()
                .id("http://example.org/record/1")
                .title("Test Record")
                .description("A test record")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .build();
        
        record.setResource(dataset);
        catalog.addRecord(record);
        
        // Create and add a subcatalog
        Catalog subcatalog = Catalog.builder()
                .id("http://example.org/catalog/2")
                .title("Subcatalog")
                .description("A subcatalog")
                .issued(ZonedDateTime.now())
                .modified(ZonedDateTime.now())
                .build();
        
        catalog.addCatalog(subcatalog);
        
        // Verify the catalog
        assertEquals("http://example.org/catalog/1", catalog.getId());
        assertEquals("Complete Catalog", catalog.getTitle());
        assertEquals("A complete catalog with datasets, services, and records", catalog.getDescription());
        assertEquals(1, catalog.getDatasets().size());
        assertEquals(1, catalog.getServices().size());
        assertEquals(1, catalog.getRecords().size());
        assertEquals(1, catalog.getCatalogs().size());
        
        // Verify the dataset
        Dataset retrievedDataset = catalog.getDatasets().iterator().next();
        assertEquals("http://example.org/dataset/1", retrievedDataset.getId());
        assertEquals("Test Dataset", retrievedDataset.getTitle());
        assertEquals(1, retrievedDataset.getDistributions().size());
        
        // Verify the service
        DataService retrievedService = catalog.getServices().iterator().next();
        assertEquals("http://example.org/service/1", retrievedService.getId());
        assertEquals("Test Service", retrievedService.getTitle());
        assertEquals(1, retrievedService.getServesDatasets().size());
        
        // Verify the record
        CatalogRecord retrievedRecord = catalog.getRecords().iterator().next();
        assertEquals("http://example.org/record/1", retrievedRecord.getId());
        assertEquals("Test Record", retrievedRecord.getTitle());
        assertNotNull(retrievedRecord.getResource());
        
        // Verify the subcatalog
        Catalog retrievedSubcatalog = catalog.getCatalogs().iterator().next();
        assertEquals("http://example.org/catalog/2", retrievedSubcatalog.getId());
        assertEquals("Subcatalog", retrievedSubcatalog.getTitle());
    }
}