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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.util.Date;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;

public class Dcat2SerializationService {

    private static final String BASE_URI = "http://example.com/"; // This should be configurable
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // ISO 8601 format
    private static final String SHACL_SHAPES_FILE = "dcat2-shacl.ttl"; // Path to the SHACL shapes file

    /**
     * Converts a Catalog object to a Jena Model (RDF graph).
     *
     * @param catalog The Catalog object to convert.
     * @return A Jena Model representing the Catalog.
     */
    public Model toModel(Catalog catalog) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("dcat", DCAT.getURI());
        model.setNsPrefix("dct", DCTerms.getURI());
        model.setNsPrefix("rdf", RDF.getURI());

        Resource catalogResource = model.createResource(BASE_URI + "catalog/" + catalog.getId(), DCAT.Catalog);

        // Add basic properties
        if (catalog.getTitle() != null) {
            catalogResource.addProperty(DCTerms.title, catalog.getTitle());
        }
        if (catalog.getDescription() != null) {
            catalogResource.addProperty(DCTerms.description, catalog.getDescription());
        }
        if (catalog.getPublisher() != null) {
            catalogResource.addProperty(DCTerms.publisher, catalog.getPublisher()); // Publisher as literal for now
        }
        if (catalog.getIssued() != null) {
            catalogResource.addProperty(DCTerms.issued, DATE_FORMAT.format(catalog.getIssued())); // Convert Date to String
        }
        if (catalog.getModified() != null) {
            catalogResource.addProperty(DCTerms.modified, DATE_FORMAT.format(catalog.getModified())); // Convert Date to String
        }
        if (catalog.getLicense() != null) {
            catalogResource.addProperty(DCTerms.license, model.createResource(catalog.getLicense().toString())); // License as Resource
        }
         if (catalog.getThemeTaxonomy() != null) {
            for (URI themeUri : catalog.getThemeTaxonomy()) {
                catalogResource.addProperty(DCAT.themeTaxonomy, model.createResource(themeUri.toString()));
            }
        }

        // Add datasets
        if (catalog.getDatasets() != null) {
            for (Dataset dataset : catalog.getDatasets()) {
                Resource datasetResource = toModel(model, dataset);
                catalogResource.addProperty(DCAT.dataset, datasetResource);
            }
        }

        return model;
    }

    /**
     * Converts a Dataset object to a Jena Resource.
     *
     * @param model The Jena Model to add the resource to.
     * @param dataset The Dataset object to convert.
     * @return A Jena Resource representing the Dataset.
     */
    public Resource toModel(Model model, Dataset dataset) {
        Resource datasetResource = model.createResource(BASE_URI + "dataset/" + dataset.getId(), DCAT.Dataset);

        // Add basic properties
        if (dataset.getTitle() != null) {
            datasetResource.addProperty(DCTerms.title, dataset.getTitle());
        }
        if (dataset.getDescription() != null) {
            datasetResource.addProperty(DCTerms.description, dataset.getDescription());
        }
         if (dataset.getPublisher() != null) {
            datasetResource.addProperty(DCTerms.publisher, dataset.getPublisher()); // Publisher as literal for now
        }
        if (dataset.getIssued() != null) {
            datasetResource.addProperty(DCTerms.issued, DATE_FORMAT.format(dataset.getIssued())); // Convert Date to String
        }
        if (dataset.getModified() != null) {
            datasetResource.addProperty(DCTerms.modified, DATE_FORMAT.format(dataset.getModified())); // Convert Date to String
        }
        if (dataset.getLicense() != null) {
            datasetResource.addProperty(DCTerms.license, model.createResource(dataset.getLicense().toString())); // License as Resource
        }
        if (dataset.getKeyword() != null) {
            for (String keyword : dataset.getKeyword()) {
                datasetResource.addProperty(DCAT.keyword, keyword);
            }
        }
        if (dataset.getTheme() != null) {
            for (URI themeUri : dataset.getTheme()) {
                datasetResource.addProperty(DCAT.theme, model.createResource(themeUri.toString()));
            }
        }
        if (dataset.getContactPoint() != null) {
            datasetResource.addProperty(DCAT.contactPoint, dataset.getContactPoint()); // ContactPoint as literal for now
        }
        if (dataset.getSpatialResolutionInMeters() != null) {
            datasetResource.addProperty(DCAT.spatialResolutionInMeters, dataset.getSpatialResolutionInMeters().toString()); // Convert Double to String
        }

        // Add distributions
        if (dataset.getDistributions() != null) {
            for (Distribution distribution : dataset.getDistributions()) {
                Resource distributionResource = toModel(model, distribution);
                datasetResource.addProperty(DCAT.distribution, distributionResource);
            }
        }

        return datasetResource;
    }

    /**
     * Converts a Distribution object to a Jena Resource.
     *
     * @param model The Jena Model to add the resource to.
     * @param distribution The Distribution object to convert.
     * @return A Jena Resource representing the Distribution.
     */
    public Resource toModel(Model model, Distribution distribution) {
        Resource distributionResource = model.createResource(BASE_URI + "distribution/" + distribution.getId(), DCAT.Distribution);

        // Add basic properties
        if (distribution.getTitle() != null) {
            distributionResource.addProperty(DCTerms.title, distribution.getTitle());
        }
        if (distribution.getDescription() != null) {
            distributionResource.addProperty(DCTerms.description, distribution.getDescription());
        }
        if (distribution.getFormat() != null) {
            distributionResource.addProperty(DCTerms.format, distribution.getFormat()); // Format as literal for now
        }
         if (distribution.getMediaType() != null) {
            distributionResource.addProperty(DCAT.mediaType, distribution.getMediaType()); // MediaType as literal for now
        }
         if (distribution.getDownloadURL() != null) {
            distributionResource.addProperty(DCAT.downloadURL, model.createResource(distribution.getDownloadURL().toString())); // DownloadURL as Resource
        }
        if (distribution.getByteSize() != null) {
            distributionResource.addProperty(DCAT.byteSize, distribution.getByteSize().toString()); // Convert Long to String
        }
        if (distribution.getChecksum() != null) {
            distributionResource.addProperty(DCAT.checksum, distribution.getChecksum()); // Checksum as literal for now
        }
        if (distribution.getAccessURL() != null) {
            distributionResource.addProperty(DCAT.accessURL, model.createResource(distribution.getAccessURL().toString())); // AccessURL as Resource
        }

        return distributionResource;
    }

    /**
     * Converts a Jena Model (RDF graph) to a Catalog object.
     *
     * @param model The Jena Model to convert.
     * @return A Catalog object created from the Model, or null if no Catalog resource is found.
     */
    public Catalog fromModel(Model model) {
        Resource catalogResource = model.listResourcesWithProperty(RDF.type, DCAT.Catalog).nextResource();
        if (catalogResource == null) {
            return null; // No Catalog resource found
        }

        Catalog catalog = new Catalog();
        // Extract ID from URI (assuming it's the last segment)
        String uri = catalogResource.getURI();
        if (uri != null && uri.startsWith(BASE_URI + "catalog/")) {
             catalog.setId(uri.substring((BASE_URI + "catalog/").length()));
        }

        // Extract properties
        if (catalogResource.hasProperty(DCTerms.title)) {
            catalog.setTitle(catalogResource.getProperty(DCTerms.title).getString());
        }
        if (catalogResource.hasProperty(DCTerms.description)) {
            catalog.setDescription(catalogResource.getProperty(DCTerms.description).getString());
        }
        if (catalogResource.hasProperty(DCTerms.publisher)) {
            catalog.setPublisher(catalogResource.getProperty(DCTerms.publisher).getString());
        }
        if (catalogResource.hasProperty(DCTerms.issued)) {
            try {
                catalog.setIssued(DATE_FORMAT.parse(catalogResource.getProperty(DCTerms.issued).getString()));
            } catch (ParseException e) {
                // Handle parsing error
                e.printStackTrace(); // Or log the error
            }
        }
        if (catalogResource.hasProperty(DCTerms.modified)) {
             try {
                catalog.setModified(DATE_FORMAT.parse(catalogResource.getProperty(DCTerms.modified).getString()));
            } catch (ParseException e) {
                // Handle parsing error
                e.printStackTrace(); // Or log the error
            }
        }
        if (catalogResource.hasProperty(DCTerms.license)) {
             try {
                catalog.setLicense(new URI(catalogResource.getProperty(DCTerms.license).getResource().getURI()));
            } catch (Exception e) {
                // Handle URI creation error
                e.printStackTrace(); // Or log the error
            }
        }
        List<URI> themeTaxonomy = new ArrayList<>();
        StmtIterator themeTaxonomyStatements = catalogResource.listProperties(DCAT.themeTaxonomy);
        while (themeTaxonomyStatements.hasNext()) {
            Statement stmt = themeTaxonomyStatements.nextStatement();
            if (stmt.getObject().isResource()) {
                 try {
                    themeTaxonomy.add(new URI(stmt.getObject().asResource().getURI()));
                 } catch (URISyntaxException e) {
                     e.printStackTrace();
                 }
            }
        }
        catalog.setThemeTaxonomy(themeTaxonomy);

        // Extract datasets
        List<Dataset> datasets = new ArrayList<>();
        StmtIterator datasetStatements = catalogResource.listProperties(DCAT.dataset);
        while (datasetStatements.hasNext()) {
            Statement stmt = datasetStatements.nextStatement();
            if (stmt.getObject().isResource()) {
                Resource datasetResource = stmt.getObject().asResource();
                Dataset dataset = fromModel(datasetResource);
                if (dataset != null) {
                    datasets.add(dataset);
                }
            }
        }
        catalog.setDatasets(datasets);

        return catalog;
    }

    /**
     * Converts a Jena Resource (representing a dcat:Dataset) to a Dataset object.
     *
     * @param datasetResource The Jena Resource to convert.
     * @return A Dataset object created from the Resource, or null if conversion fails.
     */
     private Dataset fromModel(Resource datasetResource) {
         if (!datasetResource.hasProperty(RDF.type, DCAT.Dataset)) {
             return null; // Not a Dataset resource
         }

         Dataset dataset = new Dataset();
         // Extract ID from URI
         String uri = datasetResource.getURI();
         if (uri != null && uri.startsWith(BASE_URI + "dataset/")) {
              dataset.setId(uri.substring((BASE_URI + "dataset/").length()));
         }

         // Extract properties
        if (datasetResource.hasProperty(DCTerms.title)) {
            dataset.setTitle(datasetResource.getProperty(DCTerms.title).getString());
        }
        if (datasetResource.hasProperty(DCTerms.description)) {
            dataset.setDescription(datasetResource.getProperty(DCTerms.description).getString());
        }
         if (datasetResource.hasProperty(DCTerms.publisher)) {
            dataset.setPublisher(datasetResource.getProperty(DCTerms.publisher).getString());
        }
        if (datasetResource.hasProperty(DCTerms.issued)) {
             try {
                dataset.setIssued(DATE_FORMAT.parse(datasetResource.getProperty(DCTerms.issued).getString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (datasetResource.hasProperty(DCTerms.modified)) {
             try {
                dataset.setModified(DATE_FORMAT.parse(datasetResource.getProperty(DCTerms.modified).getString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (datasetResource.hasProperty(DCTerms.license)) {
             try {
                dataset.setLicense(new URI(datasetResource.getProperty(DCTerms.license).getResource().getURI()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<String> keyword = new ArrayList<>();
        StmtIterator keywordStatements = datasetResource.listProperties(DCAT.keyword);
        while (keywordStatements.hasNext()) {
             keyword.add(keywordStatements.nextStatement().getString());
        }
        dataset.setKeyword(keyword);

        List<URI> theme = new ArrayList<>();
        StmtIterator themeStatements = datasetResource.listProperties(DCAT.theme);
        while (themeStatements.hasNext()) {
             Statement stmt = themeStatements.nextStatement();
             if (stmt.getObject().isResource()) {
                  try {
                     theme.add(new URI(stmt.getObject().asResource().getURI()));
                  } catch (URISyntaxException e) {
                      e.printStackTrace();
                  }
             }
        }
        dataset.setTheme(theme);

        if (datasetResource.hasProperty(DCAT.contactPoint)) {
            dataset.setContactPoint(datasetResource.getProperty(DCAT.contactPoint).getString());
        }
        if (datasetResource.hasProperty(DCAT.spatialResolutionInMeters)) {
            try {
                dataset.setSpatialResolutionInMeters(Double.parseDouble(datasetResource.getProperty(DCAT.spatialResolutionInMeters).getString()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

         // Extract distributions
        List<Distribution> distributions = new ArrayList<>();
        StmtIterator distributionStatements = datasetResource.listProperties(DCAT.distribution);
        while (distributionStatements.hasNext()) {
            Statement stmt = distributionStatements.nextStatement();
            if (stmt.getObject().isResource()) {
                Resource distributionResource = stmt.getObject().asResource();
                Distribution distribution = fromModel(distributionResource);
                 if (distribution != null) {
                    distributions.add(distribution);
                }
            }
        }
        dataset.setDistributions(distributions);

         return dataset;
     }

    /**
     * Converts a Jena Resource (representing a dcat:Distribution) to a Distribution object.
     *
     * @param distributionResource The Jena Resource to convert.
     * @return A Distribution object created from the Resource, or null if conversion fails.
     */
    private Distribution fromModel(Resource distributionResource) {
         if (!distributionResource.hasProperty(RDF.type, DCAT.Distribution)) {
             return null; // Not a Distribution resource
         }

         Distribution distribution = new Distribution();
         // Extract ID from URI
         String uri = distributionResource.getURI();
         if (uri != null && uri.startsWith(BASE_URI + "distribution/")) {
              distribution.setId(uri.substring((BASE_URI + "distribution/").length()));
         }

         // Extract properties
        if (distributionResource.hasProperty(DCTerms.title)) {
            distribution.setTitle(distributionResource.getProperty(DCTerms.title).getString());
        }
        if (distributionResource.hasProperty(DCTerms.description)) {
            distribution.setDescription(distributionResource.getProperty(DCTerms.description).getString());
        }
         if (distributionResource.hasProperty(DCTerms.format)) {
            distribution.setFormat(distributionResource.getProperty(DCTerms.format).getString());
        }
         if (distributionResource.hasProperty(DCAT.mediaType)) {
            distribution.setMediaType(distributionResource.getProperty(DCAT.mediaType).getString());
        }
         if (distributionResource.hasProperty(DCAT.downloadURL)) {
             try {
                distribution.setDownloadURL(new URI(distributionResource.getProperty(DCAT.downloadURL).getResource().getURI()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (distributionResource.hasProperty(DCAT.byteSize)) {
             try {
                 distribution.setByteSize(Long.parseLong(distributionResource.getProperty(DCAT.byteSize).getString()));
             } catch (NumberFormatException e) {
                 e.printStackTrace();
             }
        }
        if (distributionResource.hasProperty(DCAT.checksum)) {
            distribution.setChecksum(distributionResource.getProperty(DCAT.checksum).getString());
        }
         if (distributionResource.hasProperty(DCAT.accessURL)) {
             try {
                distribution.setAccessURL(new URI(distributionResource.getProperty(DCAT.accessURL).getResource().getURI()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

         return distribution;
    }

    /**
     * Validates a Jena Model against the predefined SHACL shapes.
     *
     * @param dataModel The Jena Model containing the data to validate.
     * @return A ValidationReport containing the validation results.
     */
    public ValidationReport validate(Model dataModel) {
        // Load the SHACL shapes graph
        InputStream shapesIs = FileManager.get().open(SHACL_SHAPES_FILE);
        if (shapesIs == null) {
            throw new IllegalArgumentException("SHACL shapes file not found: " + SHACL_SHAPES_FILE);
        }
        Model shapesModel = ModelFactory.createDefaultModel();
        shapesModel.read(shapesIs, null, "TURTLE"); // Assuming the shapes file is in Turtle format

        // Create a SHACL validator and validate the data model
        ValidationReport report = ShaclValidator.get().validate(shapesModel, dataModel);

        return report;
    }

    /**
     * Serializes a Jena Model to a specified RDF format.
     *
     * @param model The Jena Model to serialize.
     * @param lang The RDF language/format to serialize to (e.g., Lang.TURTLE, Lang.RDFXML, Lang.JSONLD).
     * @return A String representation of the serialized model.
     */
    public String serializeModel(Model model, Lang lang) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        RDFDataMgr.write(os, model, lang);
        return os.toString();
    }

    /**
     * Deserializes an RDF String in a specified format to a Jena Model.
     *
     * @param rdfString The RDF String to deserialize.
     * @param lang The RDF language/format of the input String.
     * @return A Jena Model created from the RDF String.
     */
    public Model deserializeModel(String rdfString, Lang lang) {
        Model model = ModelFactory.createDefaultModel();
        InputStream is = new ByteArrayInputStream(rdfString.getBytes());
        RDFDataMgr.read(model, is, null, lang);
        return model;
    }
} 