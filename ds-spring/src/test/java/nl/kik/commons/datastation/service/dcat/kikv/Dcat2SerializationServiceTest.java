package nl.kik.commons.datastation.service.dcat.kikv;

import nl.kik.commons.datastation.dto.dcat.kikv.Catalog;
import nl.kik.commons.datastation.dto.dcat.kikv.Dataset;
import nl.kik.commons.datastation.dto.dcat.kikv.Distribution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.util.FileManager;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class Dcat2SerializationServiceTest {

    private Dcat2SerializationService serializationService;
    private static final String BASE_URI = "http://example.com/"; // Should match the service's BASE_URI
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @BeforeEach
    void setUp() {
        serializationService = new Dcat2SerializationService();
    }

    @Test
    void testToModel() throws URISyntaxException {
        // Create sample DCAT2 objects
        Distribution distribution = new Distribution("dist-1", "Sample Distribution", "This is a sample distribution.");
        distribution.setFormat("CSV");
        distribution.setMediaType("text/csv");
        distribution.setDownloadURL(new URI(BASE_URI + "data/sample.csv"));

        Dataset dataset = new Dataset("ds-1", "Sample Dataset", "This is a sample dataset.");
        dataset.setPublisher("Sample Publisher");
        dataset.setIssued(new Date());
        dataset.setDistributions(List.of(distribution));

        Catalog catalog = new Catalog("cat-1", "Sample Catalog", "This is a sample catalog.");
        catalog.setPublisher("Catalog Publisher");
        catalog.setModified(new Date());
        catalog.setDatasets(List.of(dataset));

        // Convert to Model
        Model model = serializationService.toModel(catalog);

        // Assertions (basic checks for now)
        assertNotNull(model);
        // Check if the catalog resource exists
        Resource catalogResource = model.getResource(BASE_URI + "catalog/cat-1");
        assertNotNull(catalogResource);
        assertTrue(catalogResource.hasProperty(RDF.type, DCAT.Catalog));
        assertTrue(catalogResource.hasProperty(DCTerms.title, "Sample Catalog"));
        assertTrue(catalogResource.hasProperty(DCTerms.description, "This is a sample catalog."));

        // Check if the dataset resource exists and is linked to the catalog
        Resource datasetResource = model.getResource(BASE_URI + "dataset/ds-1");
        assertNotNull(datasetResource);
        assertTrue(datasetResource.hasProperty(RDF.type, DCAT.Dataset));
        assertTrue(catalogResource.hasProperty(DCAT.dataset, datasetResource));

        // Check if the distribution resource exists and is linked to the dataset
        Resource distributionResource = model.getResource(BASE_URI + "distribution/dist-1");
        assertNotNull(distributionResource);
        assertTrue(distributionResource.hasProperty(RDF.type, DCAT.Distribution));
        assertTrue(datasetResource.hasProperty(DCAT.distribution, distributionResource));

        // You can add more specific assertions for other properties and nested objects
    }

    @Test
    void testFromModel() throws ParseException, URISyntaxException {
        // Create a sample Jena Model (representing a DCAT2 Catalog)
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dcat", DCAT.getURI());
        model.setNsPrefix("dct", DCTerms.getURI());

        Resource catalogResource = model.createResource(BASE_URI + "catalog/cat-1", DCAT.Catalog);
        catalogResource.addProperty(DCTerms.title, "Sample Catalog From RDF");
        catalogResource.addProperty(DCTerms.description, "This catalog was created from an RDF model.");
        catalogResource.addProperty(DCTerms.publisher, "RDF Publisher");
        catalogResource.addProperty(DCTerms.issued, DATE_FORMAT.format(new Date()));
        catalogResource.addProperty(DCTerms.license, model.createResource("http://example.com/licenses/A"));

        Resource datasetResource = model.createResource(BASE_URI + "dataset/ds-1", DCAT.Dataset);
        datasetResource.addProperty(DCTerms.title, "Sample Dataset From RDF");
        datasetResource.addProperty(DCTerms.description, "This dataset was created from an RDF model.");
        catalogResource.addProperty(DCAT.dataset, datasetResource);

        Resource distributionResource = model.createResource(BASE_URI + "distribution/dist-1", DCAT.Distribution);
        distributionResource.addProperty(DCTerms.title, "Sample Distribution From RDF");
        distributionResource.addProperty(DCTerms.description, "This distribution was created from an RDF model.");
        distributionResource.addProperty(DCTerms.format, "JSON");
        distributionResource.addProperty(DCAT.mediaType, "application/json");
        distributionResource.addProperty(DCAT.downloadURL, model.createResource(BASE_URI + "data/sample.json"));
        datasetResource.addProperty(DCAT.distribution, distributionResource);

        // Convert to Catalog object
        Catalog catalog = serializationService.fromModel(model);

        // Assertions
        assertNotNull(catalog);
        assertEquals("cat-1", catalog.getId());
        assertEquals("Sample Catalog From RDF", catalog.getTitle());
        assertEquals("This catalog was created from an RDF model.", catalog.getDescription());
        assertEquals("RDF Publisher", catalog.getPublisher());
        assertNotNull(catalog.getIssued()); // Check if parsing was successful
        assertNotNull(catalog.getLicense());
        assertEquals(new URI("http://example.com/licenses/A"), catalog.getLicense());

        List<Dataset> datasets = catalog.getDatasets();
        assertNotNull(datasets);
        assertEquals(1, datasets.size());
        Dataset dataset = datasets.get(0);
        assertEquals("ds-1", dataset.getId());
        assertEquals("Sample Dataset From RDF", dataset.getTitle());
        assertEquals("This dataset was created from an RDF model.", dataset.getDescription());

        List<Distribution> distributions = dataset.getDistributions();
        assertNotNull(distributions);
        assertEquals(1, distributions.size());
        Distribution distribution = distributions.get(0);
        assertEquals("dist-1", distribution.getId());
        assertEquals("Sample Distribution From RDF", distribution.getTitle());
        assertEquals("This distribution was created from an RDF model.", distribution.getDescription());
        assertEquals("JSON", distribution.getFormat());
        assertEquals("application/json", distribution.getMediaType());
        assertNotNull(distribution.getDownloadURL());
        assertEquals(new URI(BASE_URI + "data/sample.json"), distribution.getDownloadURL());
    }

    @Test
    void testValidate() throws URISyntaxException {
        // Create a valid Catalog object
        Catalog validCatalog = new Catalog("cat-valid", "Valid Catalog", "This is a valid catalog.");
        validCatalog.setDatasets(new ArrayList<>()); // Add an empty list to avoid null pointer in serialization

        // Convert to Model
        Model validModel = serializationService.toModel(validCatalog);

        // Validate the valid model
        ValidationReport validReport = serializationService.validate(validModel);
        assertTrue(validReport.conforms());

        // Create an invalid Catalog object (missing title)
        Catalog invalidCatalog = new Catalog("cat-invalid", null, "This catalog is missing a title.");
        invalidCatalog.setDatasets(new ArrayList<>()); // Add an empty list

        // Convert to Model
        Model invalidModel = serializationService.toModel(invalidCatalog);

        // Validate the invalid model
        ValidationReport invalidReport = serializationService.validate(invalidModel);
        assertFalse(invalidReport.conforms());

        // You can add more specific assertions about the validation results if needed
        // System.out.println(invalidReport.text(new StringWriter()).toString()); // Uncomment to see validation details
    }
} 