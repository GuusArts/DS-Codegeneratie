package nl.kik.commons.dcat.service;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.dcat.model.Catalog;
import nl.kik.commons.dcat.model.CatalogRecord;
import nl.kik.commons.dcat.model.DataService;
import nl.kik.commons.dcat.model.Dataset;
import nl.kik.commons.dcat.model.Distribution;
import nl.kik.commons.dcat.vocabulary.DCAT2;
import nl.kik.commons.dto.Graph;

/**
 * Implementation of DCAT2Service using Apache Jena.
 */
@Service
@Slf4j
public class DCAT2ServiceImpl implements DCAT2Service {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public Resource save(Graph<?> graph, Catalog catalog) {
        Model model = graph.getModel();
        Resource resource = createResource(model, catalog.getId(), DCAT2.CATALOG);

        // Add basic properties
        addLiteral(model, resource, DCTerms.title, catalog.getTitle());
        addLiteral(model, resource, DCTerms.description, catalog.getDescription());
        addDateTime(model, resource, DCTerms.issued, catalog.getIssued());
        addDateTime(model, resource, DCTerms.modified, catalog.getModified());
        addLiterals(model, resource, DCTerms.language, catalog.getLanguage());
        addResource(model, resource, DCTerms.license, catalog.getLicense());
        addResource(model, resource, DCTerms.publisher, catalog.getPublisher());
        addResource(model, resource, DCTerms.spatial, catalog.getSpatial());
        addResources(model, resource, createProperty(model, DCAT2.THEME_TAXONOMY), catalog.getThemeTaxonomy());

        // Add datasets
        if (catalog.getDataset() != null) {
            for (Dataset dataset : catalog.getDataset()) {
                Resource datasetResource = save(graph, dataset);
                model.add(resource, createProperty(model, DCAT2.DATASET_PROPERTY), datasetResource);
            }
        }

        // Add services
        if (catalog.getService() != null) {
            for (DataService service : catalog.getService()) {
                Resource serviceResource = save(graph, service);
                model.add(resource, createProperty(model, DCAT2.SERVICE_PROPERTY), serviceResource);
            }
        }

        // Add records
        if (catalog.getRecord() != null) {
            for (CatalogRecord record : catalog.getRecord()) {
                Resource recordResource = save(graph, record);
                model.add(resource, createProperty(model, DCAT2.RECORD_PROPERTY), recordResource);
            }
        }

        return resource;
    }

    @Override
    public Resource save(Graph<?> graph, Dataset dataset) {
        Model model = graph.getModel();
        Resource resource = createResource(model, dataset.getId(), DCAT2.DATASET);

        // Add basic properties
        addLiteral(model, resource, DCTerms.title, dataset.getTitle());
        addLiteral(model, resource, DCTerms.description, dataset.getDescription());
        addDateTime(model, resource, DCTerms.issued, dataset.getIssued());
        addDateTime(model, resource, DCTerms.modified, dataset.getModified());
        addResource(model, resource, DCTerms.accrualPeriodicity, dataset.getAccrualPeriodicity());
        addLiterals(model, resource, DCTerms.language, dataset.getLanguage());
        addResource(model, resource, DCTerms.license, dataset.getLicense());
        addResource(model, resource, DCTerms.publisher, dataset.getPublisher());
        addResource(model, resource, DCTerms.spatial, dataset.getSpatial());
        addLiteral(model, resource, DCTerms.temporal, dataset.getTemporal());
        addLiterals(model, resource, createProperty(model, DCAT2.KEYWORD), dataset.getKeyword());
        addResources(model, resource, createProperty(model, DCAT2.THEME), dataset.getTheme());
        addResource(model, resource, createProperty(model, DCAT2.LANDING_PAGE), dataset.getLandingPage());
        addResource(model, resource, createProperty(model, DCAT2.CONTACT_POINT), dataset.getContactPoint());

        // Add distributions
        if (dataset.getDistribution() != null) {
            for (Distribution distribution : dataset.getDistribution()) {
                Resource distributionResource = save(graph, distribution);
                model.add(resource, createProperty(model, DCAT2.DISTRIBUTION_PROPERTY), distributionResource);
            }
        }

        return resource;
    }

    @Override
    public Resource save(Graph<?> graph, Distribution distribution) {
        Model model = graph.getModel();
        Resource resource = createResource(model, distribution.getId(), DCAT2.DISTRIBUTION);

        // Add basic properties
        addLiteral(model, resource, DCTerms.title, distribution.getTitle());
        addLiteral(model, resource, DCTerms.description, distribution.getDescription());
        addDateTime(model, resource, DCTerms.issued, distribution.getIssued());
        addDateTime(model, resource, DCTerms.modified, distribution.getModified());
        addResource(model, resource, DCTerms.license, distribution.getLicense());
        addResource(model, resource, DCTerms.rights, distribution.getRights());
        addResource(model, resource, createProperty(model, DCAT2.ACCESS_URL), distribution.getAccessURL());
        addResource(model, resource, createProperty(model, DCAT2.DOWNLOAD_URL), distribution.getDownloadURL());
        addLiteral(model, resource, createProperty(model, DCAT2.BYTE_SIZE), distribution.getByteSize());
        addLiteral(model, resource, createProperty(model, DCAT2.MEDIA_TYPE), distribution.getMediaType());
        addLiteral(model, resource, createProperty(model, DCAT2.FORMAT), distribution.getFormat());
        addResource(model, resource, createProperty(model, DCAT2.COMPRESS_FORMAT), distribution.getCompressFormat());
        addResource(model, resource, createProperty(model, DCAT2.PACKAGE_FORMAT), distribution.getPackageFormat());

        return resource;
    }

    @Override
    public Resource save(Graph<?> graph, DataService dataService) {
        Model model = graph.getModel();
        Resource resource = createResource(model, dataService.getId(), DCAT2.DATA_SERVICE);

        // Add basic properties
        addLiteral(model, resource, DCTerms.title, dataService.getTitle());
        addLiteral(model, resource, DCTerms.description, dataService.getDescription());
        addDateTime(model, resource, DCTerms.issued, dataService.getIssued());
        addDateTime(model, resource, DCTerms.modified, dataService.getModified());
        addResource(model, resource, DCTerms.license, dataService.getLicense());
        addResource(model, resource, DCTerms.publisher, dataService.getPublisher());
        addResource(model, resource, createProperty(model, DCAT2.ENDPOINT_URL), dataService.getEndpointURL());
        addResource(model, resource, createProperty(model, DCAT2.ENDPOINT_DESCRIPTION), dataService.getEndpointDescription());

        // Add served datasets
        if (dataService.getServesDataset() != null) {
            for (Dataset dataset : dataService.getServesDataset()) {
                Resource datasetResource = save(graph, dataset);
                model.add(resource, createProperty(model, DCAT2.SERVES_DATASET), datasetResource);
            }
        }

        return resource;
    }

    @Override
    public Resource save(Graph<?> graph, CatalogRecord catalogRecord) {
        Model model = graph.getModel();
        Resource resource = createResource(model, catalogRecord.getId(), DCAT2.CATALOG_RECORD);

        // Add basic properties
        addDateTime(model, resource, DCTerms.issued, catalogRecord.getIssued());
        addDateTime(model, resource, DCTerms.modified, catalogRecord.getModified());
        addResource(model, resource, DCTerms.conformsTo, catalogRecord.getConformsTo());
        addLiteral(model, resource, DCTerms.title, catalogRecord.getTitle());
        addLiteral(model, resource, DCTerms.description, catalogRecord.getDescription());
        addResource(model, resource, createProperty(model, "http://xmlns.com/foaf/0.1/primaryTopic"), catalogRecord.getPrimaryTopic());

        return resource;
    }

    @Override
    public Catalog loadCatalog(Graph<?> graph, String uri) {
        Model model = graph.getModel();
        Resource resource = model.getResource(uri);

        if (!resource.hasProperty(RDF.type, model.createResource(DCAT2.CATALOG))) {
            log.warn("Resource {} is not a dcat:Catalog", uri);
            return null;
        }

        Catalog.CatalogBuilder<?, ?> builder = Catalog.builder()
                .id(uri)
                .title(getLiteralValue(resource, DCTerms.title))
                .description(getLiteralValue(resource, DCTerms.description))
                .issued(getDateTimeValue(resource, DCTerms.issued))
                .modified(getDateTimeValue(resource, DCTerms.modified))
                .language(getLiteralValues(resource, DCTerms.language))
                .homepage(getResourceURI(resource, "http://xmlns.com/foaf/0.1/homepage"))
                .license(getResourceURI(resource, DCTerms.license))
                .publisher(getResourceURI(resource, DCTerms.publisher))
                .spatial(getResourceURI(resource, DCTerms.spatial))
                .themeTaxonomy(getResourceURIs(resource, createProperty(model, DCAT2.THEME_TAXONOMY)));

        // Load datasets
        List<Dataset> datasets = new ArrayList<>();
        StmtIterator datasetIterator = resource.listProperties(createProperty(model, DCAT2.DATASET_PROPERTY));
        while (datasetIterator.hasNext()) {
            Statement stmt = datasetIterator.next();
            if (stmt.getObject().isResource()) {
                Dataset dataset = loadDataset(graph, stmt.getObject().asResource().getURI());
                if (dataset != null) {
                    datasets.add(dataset);
                }
            }
        }
        if (!datasets.isEmpty()) {
            builder.dataset(datasets);
        }

        // Load services
        List<DataService> services = new ArrayList<>();
        StmtIterator serviceIterator = resource.listProperties(createProperty(model, DCAT2.SERVICE_PROPERTY));
        while (serviceIterator.hasNext()) {
            Statement stmt = serviceIterator.next();
            if (stmt.getObject().isResource()) {
                DataService service = loadDataService(graph, stmt.getObject().asResource().getURI());
                if (service != null) {
                    services.add(service);
                }
            }
        }
        if (!services.isEmpty()) {
            builder.service(services);
        }

        // Load records
        List<CatalogRecord> records = new ArrayList<>();
        StmtIterator recordIterator = resource.listProperties(createProperty(model, DCAT2.RECORD_PROPERTY));
        while (recordIterator.hasNext()) {
            Statement stmt = recordIterator.next();
            if (stmt.getObject().isResource()) {
                CatalogRecord record = loadCatalogRecord(graph, stmt.getObject().asResource().getURI());
                if (record != null) {
                    records.add(record);
                }
            }
        }
        if (!records.isEmpty()) {
            builder.record(records);
        }

        return builder.build();
    }

    @Override
    public Dataset loadDataset(Graph<?> graph, String uri) {
        Model model = graph.getModel();
        Resource resource = model.getResource(uri);

        if (!resource.hasProperty(RDF.type, model.createResource(DCAT2.DATASET))) {
            log.warn("Resource {} is not a dcat:Dataset", uri);
            return null;
        }

        Dataset.DatasetBuilder<?, ?> builder = Dataset.builder()
                .id(uri)
                .title(getLiteralValue(resource, DCTerms.title))
                .description(getLiteralValue(resource, DCTerms.description))
                .issued(getDateTimeValue(resource, DCTerms.issued))
                .modified(getDateTimeValue(resource, DCTerms.modified))
                .accrualPeriodicity(getResourceURI(resource, DCTerms.accrualPeriodicity))
                .language(getLiteralValues(resource, DCTerms.language))
                .license(getResourceURI(resource, DCTerms.license))
                .publisher(getResourceURI(resource, DCTerms.publisher))
                .spatial(getResourceURI(resource, DCTerms.spatial))
                .temporal(getLiteralValue(resource, DCTerms.temporal))
                .keyword(getLiteralValues(resource, createProperty(model, DCAT2.KEYWORD)))
                .theme(getResourceURIs(resource, createProperty(model, DCAT2.THEME)))
                .landingPage(getResourceURI(resource, createProperty(model, DCAT2.LANDING_PAGE)))
                .contactPoint(getResourceURI(resource, createProperty(model, DCAT2.CONTACT_POINT)));

        // Load distributions
        List<Distribution> distributions = new ArrayList<>();
        StmtIterator distributionIterator = resource.listProperties(createProperty(model, DCAT2.DISTRIBUTION_PROPERTY));
        while (distributionIterator.hasNext()) {
            Statement stmt = distributionIterator.next();
            if (stmt.getObject().isResource()) {
                Distribution distribution = loadDistribution(graph, stmt.getObject().asResource().getURI());
                if (distribution != null) {
                    distributions.add(distribution);
                }
            }
        }
        if (!distributions.isEmpty()) {
            builder.distribution(distributions);
        }

        return builder.build();
    }

    @Override
    public Distribution loadDistribution(Graph<?> graph, String uri) {
        Model model = graph.getModel();
        Resource resource = model.getResource(uri);

        if (!resource.hasProperty(RDF.type, model.createResource(DCAT2.DISTRIBUTION))) {
            log.warn("Resource {} is not a dcat:Distribution", uri);
            return null;
        }

        return Distribution.builder()
                .id(uri)
                .title(getLiteralValue(resource, DCTerms.title))
                .description(getLiteralValue(resource, DCTerms.description))
                .issued(getDateTimeValue(resource, DCTerms.issued))
                .modified(getDateTimeValue(resource, DCTerms.modified))
                .license(getResourceURI(resource, DCTerms.license))
                .rights(getResourceURI(resource, DCTerms.rights))
                .accessURL(getResourceURI(resource, createProperty(model, DCAT2.ACCESS_URL)))
                .downloadURL(getResourceURI(resource, createProperty(model, DCAT2.DOWNLOAD_URL)))
                .byteSize(getLongValue(resource, createProperty(model, DCAT2.BYTE_SIZE)))
                .mediaType(getLiteralValue(resource, createProperty(model, DCAT2.MEDIA_TYPE)))
                .format(getLiteralValue(resource, createProperty(model, DCAT2.FORMAT)))
                .compressFormat(getResourceURI(resource, createProperty(model, DCAT2.COMPRESS_FORMAT)))
                .packageFormat(getResourceURI(resource, createProperty(model, DCAT2.PACKAGE_FORMAT)))
                .build();
    }

    @Override
    public DataService loadDataService(Graph<?> graph, String uri) {
        Model model = graph.getModel();
        Resource resource = model.getResource(uri);

        if (!resource.hasProperty(RDF.type, model.createResource(DCAT2.DATA_SERVICE))) {
            log.warn("Resource {} is not a dcat:DataService", uri);
            return null;
        }

        DataService.DataServiceBuilder<?, ?> builder = DataService.builder()
                .id(uri)
                .title(getLiteralValue(resource, DCTerms.title))
                .description(getLiteralValue(resource, DCTerms.description))
                .issued(getDateTimeValue(resource, DCTerms.issued))
                .modified(getDateTimeValue(resource, DCTerms.modified))
                .license(getResourceURI(resource, DCTerms.license))
                .publisher(getResourceURI(resource, DCTerms.publisher))
                .endpointURL(getResourceURI(resource, createProperty(model, DCAT2.ENDPOINT_URL)))
                .endpointDescription(getResourceURI(resource, createProperty(model, DCAT2.ENDPOINT_DESCRIPTION)));

        // Load served datasets
        List<Dataset> datasets = new ArrayList<>();
        StmtIterator datasetIterator = resource.listProperties(createProperty(model, DCAT2.SERVES_DATASET));
        while (datasetIterator.hasNext()) {
            Statement stmt = datasetIterator.next();
            if (stmt.getObject().isResource()) {
                Dataset dataset = loadDataset(graph, stmt.getObject().asResource().getURI());
                if (dataset != null) {
                    datasets.add(dataset);
                }
            }
        }
        if (!datasets.isEmpty()) {
            builder.servesDataset(datasets);
        }

        return builder.build();
    }

    @Override
    public CatalogRecord loadCatalogRecord(Graph<?> graph, String uri) {
        Model model = graph.getModel();
        Resource resource = model.getResource(uri);

        if (!resource.hasProperty(RDF.type, model.createResource(DCAT2.CATALOG_RECORD))) {
            log.warn("Resource {} is not a dcat:CatalogRecord", uri);
            return null;
        }

        return CatalogRecord.builder()
                .id(uri)
                .issued(getDateTimeValue(resource, DCTerms.issued))
                .modified(getDateTimeValue(resource, DCTerms.modified))
                .conformsTo(getResourceURI(resource, DCTerms.conformsTo))
                .title(getLiteralValue(resource, DCTerms.title))
                .description(getLiteralValue(resource, DCTerms.description))
                .primaryTopic(getResourceURI(resource, createProperty(model, "http://xmlns.com/foaf/0.1/primaryTopic")))
                .build();
    }

    // Helper methods

    private Resource createResource(Model model, String uri, String type) {
        Resource resource;
        if (uri != null && !uri.isEmpty()) {
            resource = model.createResource(uri);
        } else {
            resource = model.createResource();
        }
        model.add(resource, RDF.type, model.createResource(type));
        return resource;
    }

    private Property createProperty(Model model, String uri) {
        return model.createProperty(uri);
    }

    private void addLiteral(Model model, Resource resource, Property property, String value) {
        if (value != null && !value.isEmpty()) {
            model.add(resource, property, value);
        }
    }

    private void addLiteral(Model model, Resource resource, Property property, Long value) {
        if (value != null) {
            model.add(resource, property, value);
        }
    }

    private void addLiterals(Model model, Resource resource, Property property, List<String> values) {
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    model.add(resource, property, value);
                }
            }
        }
    }

    private void addDateTime(Model model, Resource resource, Property property, ZonedDateTime dateTime) {
        if (dateTime != null) {
            model.add(resource, property, dateTime.format(DATE_TIME_FORMATTER));
        }
    }

    private void addResource(Model model, Resource resource, Property property, URI uri) {
        if (uri != null) {
            model.add(resource, property, model.createResource(uri.toString()));
        }
    }

    private void addResources(Model model, Resource resource, Property property, List<URI> uris) {
        if (uris != null) {
            for (URI uri : uris) {
                if (uri != null) {
                    model.add(resource, property, model.createResource(uri.toString()));
                }
            }
        }
    }

    private String getLiteralValue(Resource resource, Property property) {
        Statement stmt = resource.getProperty(property);
        if (stmt != null && stmt.getObject().isLiteral()) {
            return stmt.getString();
        }
        return null;
    }

    private Long getLongValue(Resource resource, Property property) {
        Statement stmt = resource.getProperty(property);
        if (stmt != null && stmt.getObject().isLiteral()) {
            try {
                return stmt.getLong();
            } catch (Exception e) {
                log.warn("Failed to parse long value for property {}: {}", property, e.getMessage());
            }
        }
        return null;
    }

    private List<String> getLiteralValues(Resource resource, Property property) {
        List<String> values = new ArrayList<>();
        StmtIterator stmts = resource.listProperties(property);
        while (stmts.hasNext()) {
            Statement stmt = stmts.next();
            if (stmt.getObject().isLiteral()) {
                values.add(stmt.getString());
            }
        }
        return values.isEmpty() ? null : values;
    }

    private ZonedDateTime getDateTimeValue(Resource resource, Property property) {
        Statement stmt = resource.getProperty(property);
        if (stmt != null && stmt.getObject().isLiteral()) {
            try {
                return ZonedDateTime.parse(stmt.getString(), DATE_TIME_FORMATTER);
            } catch (Exception e) {
                log.warn("Failed to parse date-time value for property {}: {}", property, e.getMessage());
            }
        }
        return null;
    }

    private URI getResourceURI(Resource resource, Property property) {
        Statement stmt = resource.getProperty(property);
        if (stmt != null && stmt.getObject().isResource()) {
            try {
                return URI.create(stmt.getResource().getURI());
            } catch (Exception e) {
                log.warn("Failed to create URI for property {}: {}", property, e.getMessage());
            }
        }
        return null;
    }

    private URI getResourceURI(Resource resource, String propertyURI) {
        return getResourceURI(resource, createProperty(resource.getModel(), propertyURI));
    }

    private List<URI> getResourceURIs(Resource resource, Property property) {
        List<URI> uris = new ArrayList<>();
        StmtIterator stmts = resource.listProperties(property);
        while (stmts.hasNext()) {
            Statement stmt = stmts.next();
            if (stmt.getObject().isResource()) {
                try {
                    uris.add(URI.create(stmt.getResource().getURI()));
                } catch (Exception e) {
                    log.warn("Failed to create URI for property {}: {}", property, e.getMessage());
                }
            }
        }
        return uris.isEmpty() ? null : uris;
    }
}