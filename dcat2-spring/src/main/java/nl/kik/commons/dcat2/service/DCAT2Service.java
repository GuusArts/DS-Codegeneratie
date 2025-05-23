package nl.kik.commons.dcat2.service;

import java.io.OutputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.dcat2.dto.Agent;
import nl.kik.commons.dcat2.dto.Catalog;
import nl.kik.commons.dcat2.dto.CatalogRecord;
import nl.kik.commons.dcat2.dto.DCATObject;
import nl.kik.commons.dcat2.dto.DataService;
import nl.kik.commons.dcat2.dto.Dataset;
import nl.kik.commons.dcat2.dto.Distribution;
import nl.kik.commons.dcat2.dto.Location;
import nl.kik.commons.dcat2.dto.PeriodOfTime;
import nl.kik.commons.dcat2.vocabulary.DCAT2;
import nl.kik.commons.dto.Graph;
import nl.kik.commons.service.RDFService;

/**
 * Service for converting DCAT2 model objects to RDF and vice versa.
 */
@Slf4j
public class DCAT2Service extends RDFService {

    /**
     * Convert a DCAT2 Catalog to RDF.
     *
     * @param catalog The catalog to convert
     * @return A Jena Model containing RDF representation of the catalog
     */
    public Model convertCatalogToRDF(final Catalog catalog) {
        final Model model = ModelFactory.createDefaultModel();
        
        final Resource catalogResource = createResourceWithId(model, catalog);
        model.add(catalogResource, RDF.type, model.createResource(DCAT2.CATALOG));
        
        // Add basic properties
        addStringIfNotEmpty(model, catalogResource, DCTerms.title, catalog.getTitle());
        addStringIfNotEmpty(model, catalogResource, DCTerms.description, catalog.getDescription());
        
        // Add temporal properties
        addDateTimeIfNotNull(model, catalogResource, DCTerms.issued, catalog.getIssued());
        addDateTimeIfNotNull(model, catalogResource, DCTerms.modified, catalog.getModified());
        
        // Add license
        if (catalog.getLicense() != null) {
            model.add(catalogResource, DCTerms.license, model.createResource(catalog.getLicense().toString()));
        }
        
        // Add publisher
        addAgent(model, catalogResource, DCTerms.publisher, catalog.getPublisher());
        
        // Add creator
        addAgent(model, catalogResource, DCTerms.creator, catalog.getCreator());
        
        // Add languages
        addURIsFromStrings(model, catalogResource, DCTerms.language, catalog.getLanguage());
        
        // Add theme taxonomy
        addURIs(model, catalogResource, ResourceFactory.createProperty(DCAT2.THEME_TAXONOMY), catalog.getThemeTaxonomy());
        
        // Add homepage
        addURIs(model, catalogResource, FOAF.homepage, catalog.getHomepage());
        
        // Add datasets
        if (CollectionUtils.isNotEmpty(catalog.getDatasets())) {
            for (Dataset dataset : catalog.getDatasets()) {
                final Model datasetModel = convertDatasetToRDF(dataset);
                model.add(datasetModel);
                
                final Resource datasetResource = createResourceWithId(model, dataset);
                model.add(catalogResource, model.createProperty(DCAT2.DATASET_PROP), datasetResource);
            }
        }
        
        // Add services
        if (CollectionUtils.isNotEmpty(catalog.getServices())) {
            for (DataService service : catalog.getServices()) {
                final Model serviceModel = convertDataServiceToRDF(service);
                model.add(serviceModel);
                
                final Resource serviceResource = createResourceWithId(model, service);
                model.add(catalogResource, model.createProperty(DCAT2.SERVICE), serviceResource);
            }
        }
        
        return model;
    }
    
    /**
     * Convert a DCAT2 Dataset to RDF.
     *
     * @param dataset The dataset to convert
     * @return A Jena Model containing RDF representation of the dataset
     */
    public Model convertDatasetToRDF(final Dataset dataset) {
        final Model model = ModelFactory.createDefaultModel();
        
        final Resource datasetResource = createResourceWithId(model, dataset);
        model.add(datasetResource, RDF.type, model.createResource(DCAT2.DATASET));
        
        // Add basic properties
        addStringIfNotEmpty(model, datasetResource, DCTerms.title, dataset.getTitle());
        addStringIfNotEmpty(model, datasetResource, DCTerms.description, dataset.getDescription());
        
        // Add temporal properties
        addDateTimeIfNotNull(model, datasetResource, DCTerms.issued, dataset.getIssued());
        addDateTimeIfNotNull(model, datasetResource, DCTerms.modified, dataset.getModified());
        
        // Add version
        addStringIfNotEmpty(model, datasetResource, model.createProperty(DCAT2.VERSION), dataset.getVersion());
        
        // Add license
        if (dataset.getLicense() != null) {
            model.add(datasetResource, DCTerms.license, model.createResource(dataset.getLicense().toString()));
        }
        
        // Add publisher
        addAgent(model, datasetResource, DCTerms.publisher, dataset.getPublisher());
        
        // Add creator
        addAgent(model, datasetResource, DCTerms.creator, dataset.getCreator());
        
        // Add keywords
        addStrings(model, datasetResource, model.createProperty(DCAT2.KEYWORD), dataset.getKeywords());
        
        // Add landing page
        addURIs(model, datasetResource, model.createProperty(DCAT2.LANDING_PAGE), dataset.getLandingPage());
        
        // Add theme
        addURIs(model, datasetResource, model.createProperty(DCAT2.KEYWORD), dataset.getTheme());
        
        // Add languages
        addURIsFromStrings(model, datasetResource, DCTerms.language, dataset.getLanguage());
        
        // Add accrual periodicity
        if (dataset.getAccrualPeriodicity() != null) {
            model.add(datasetResource, DCTerms.accrualPeriodicity, 
                    model.createResource(dataset.getAccrualPeriodicity().toString()));
        }
        
        // Add spatial coverage
        addLocation(model, datasetResource, DCTerms.spatial, dataset.getSpatial());
        
        // Add temporal coverage
        addPeriodOfTime(model, datasetResource, DCTerms.temporal, dataset.getTemporal());
        
        // Add in catalog reference
        if (dataset.getInCatalog() != null) {
            model.add(datasetResource, model.createProperty(DCAT2.IN_CATALOG), 
                    model.createResource(dataset.getInCatalog().toString()));
        }
        
        // Add distributions
        if (CollectionUtils.isNotEmpty(dataset.getDistributions())) {
            for (Distribution distribution : dataset.getDistributions()) {
                final Model distributionModel = convertDistributionToRDF(distribution);
                model.add(distributionModel);
                
                final Resource distributionResource = createResourceWithId(model, distribution);
                model.add(datasetResource, model.createProperty(DCAT2.DISTRIBUTION_PROP), distributionResource);
            }
        }
        
        // Add serving data services
        if (CollectionUtils.isNotEmpty(dataset.getServingDataServices())) {
            for (DataService service : dataset.getServingDataServices()) {
                final Model serviceModel = convertDataServiceToRDF(service);
                model.add(serviceModel);
                
                final Resource serviceResource = createResourceWithId(model, service);
                model.add(serviceResource, model.createProperty(DCAT2.SERVES_DATASET), datasetResource);
            }
        }
        
        return model;
    }
    
    /**
     * Convert a DCAT2 Distribution to RDF.
     *
     * @param distribution The distribution to convert
     * @return A Jena Model containing RDF representation of the distribution
     */
    public Model convertDistributionToRDF(final Distribution distribution) {
        final Model model = ModelFactory.createDefaultModel();
        
        final Resource distributionResource = createResourceWithId(model, distribution);
        model.add(distributionResource, RDF.type, model.createResource(DCAT2.DISTRIBUTION));
        
        // Add basic properties
        addStringIfNotEmpty(model, distributionResource, DCTerms.title, distribution.getTitle());
        addStringIfNotEmpty(model, distributionResource, DCTerms.description, distribution.getDescription());
        
        // Add temporal properties
        addDateTimeIfNotNull(model, distributionResource, DCTerms.issued, distribution.getIssued());
        addDateTimeIfNotNull(model, distributionResource, DCTerms.modified, distribution.getModified());
        
        // Add URLs
        if (distribution.getAccessURL() != null) {
            model.add(distributionResource, model.createProperty(DCAT2.ACCESS_URL), 
                    model.createResource(distribution.getAccessURL().toString()));
        }
        if (distribution.getDownloadURL() != null) {
            model.add(distributionResource, model.createProperty(DCAT2.DOWNLOAD_URL), 
                    model.createResource(distribution.getDownloadURL().toString()));
        }
        
        // Add format information
        addStringIfNotEmpty(model, distributionResource, model.createProperty(DCAT2.MEDIA_TYPE), distribution.getMediaType());
        addStringIfNotEmpty(model, distributionResource, DCTerms.format, distribution.getFormat());
        addStringIfNotEmpty(model, distributionResource, model.createProperty(DCAT2.COMPRESSION_FORMAT), 
                distribution.getCompressionFormat());
        addStringIfNotEmpty(model, distributionResource, model.createProperty(DCAT2.PACKAGING_FORMAT), 
                distribution.getPackageFormat());
        
        // Add byte size
        if (distribution.getByteSize() != null) {
            model.add(distributionResource, model.createProperty(DCAT2.BYTE_SIZE), 
                    model.createTypedLiteral(distribution.getByteSize().toString(), XSDDatatype.XSDlong));
        }
        
        // Add license
        if (distribution.getLicense() != null) {
            model.add(distributionResource, DCTerms.license, 
                    model.createResource(distribution.getLicense().toString()));
        }
        
        // Add rights
        if (distribution.getRights() != null) {
            model.add(distributionResource, DCTerms.rights, 
                    model.createResource(distribution.getRights().toString()));
        }
        
        // Add access service
        if (distribution.getAccessService() != null) {
            final Model serviceModel = convertDataServiceToRDF(distribution.getAccessService());
            model.add(serviceModel);
            
            final Resource serviceResource = createResourceWithId(model, distribution.getAccessService());
            model.add(distributionResource, model.createProperty(DCAT2.ACCESS_SERVICE), serviceResource);
        }
        
        return model;
    }
    
    /**
     * Convert a DCAT2 DataService to RDF.
     *
     * @param service The data service to convert
     * @return A Jena Model containing RDF representation of the data service
     */
    public Model convertDataServiceToRDF(final DataService service) {
        final Model model = ModelFactory.createDefaultModel();
        
        final Resource serviceResource = createResourceWithId(model, service);
        model.add(serviceResource, RDF.type, model.createResource(DCAT2.DATA_SERVICE));
        
        // Add basic properties
        addStringIfNotEmpty(model, serviceResource, DCTerms.title, service.getTitle());
        addStringIfNotEmpty(model, serviceResource, DCTerms.description, service.getDescription());
        
        // Add endpoint URL
        if (service.getEndpointURL() != null) {
            model.add(serviceResource, model.createProperty(DCAT2.ENDPOINT_URL), 
                    model.createResource(service.getEndpointURL().toString()));
        }
        
        // Add endpoint description
        if (service.getEndpointDescription() != null) {
            model.add(serviceResource, model.createProperty(DCAT2.ENDPOINT), 
                    model.createResource(service.getEndpointDescription().toString()));
        }
        
        // Add serves dataset
        addURIs(model, serviceResource, model.createProperty(DCAT2.SERVES_DATASET), service.getServesDataset());
        
        // Add license
        if (service.getLicense() != null) {
            model.add(serviceResource, DCTerms.license, 
                    model.createResource(service.getLicense().toString()));
        }
        
        // Add access rights
        if (service.getAccessRights() != null) {
            model.add(serviceResource, DCTerms.accessRights, 
                    model.createResource(service.getAccessRights().toString()));
        }
        
        return model;
    }
    
    /**
     * Convert a DCAT2 CatalogRecord to RDF.
     *
     * @param record The catalog record to convert
     * @return A Jena Model containing RDF representation of the catalog record
     */
    public Model convertCatalogRecordToRDF(final CatalogRecord record) {
        final Model model = ModelFactory.createDefaultModel();
        
        final Resource recordResource = createResourceWithId(model, record);
        model.add(recordResource, RDF.type, model.createResource(DCAT2.CATALOG_RECORD));
        
        // Add basic properties
        addStringIfNotEmpty(model, recordResource, DCTerms.title, record.getTitle());
        addStringIfNotEmpty(model, recordResource, DCTerms.description, record.getDescription());
        
        // Add temporal properties
        addDateTimeIfNotNull(model, recordResource, DCTerms.issued, record.getIssued());
        addDateTimeIfNotNull(model, recordResource, DCTerms.modified, record.getModified());
        
        // Add primary topic
        if (record.getPrimaryTopic() != null) {
            model.add(recordResource, FOAF.primaryTopic, 
                    model.createResource(record.getPrimaryTopic().toString()));
        }
        
        // Add conformsTo
        if (record.getConformsTo() != null) {
            model.add(recordResource, DCTerms.conformsTo, 
                    model.createResource(record.getConformsTo().toString()));
        }
        
        // Add record status
        addStringIfNotEmpty(model, recordResource, model.createProperty("http://www.w3.org/ns/adms#status"), 
                record.getRecordStatus());
        
        // Add source metadata
        if (record.getSourceMetadata() != null) {
            model.add(recordResource, DCTerms.source, 
                    model.createResource(record.getSourceMetadata().toString()));
        }
        
        return model;
    }
    
    /**
     * Create a resource for a DCAT object, with the id as URI if available.
     * 
     * @param model The model to create the resource in
     * @param object The DCAT object
     * @return The created resource
     */
    private Resource createResourceWithId(Model model, DCATObject object) {
        if (StringUtils.isNotBlank(object.getId())) {
            return model.createResource(object.getId());
        }
        return model.createResource();
    }
    
    /**
     * Add a string value to a resource if not empty.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param value The value to add
     */
    private void addStringIfNotEmpty(Model model, Resource resource, Property property, String value) {
        if (StringUtils.isNotBlank(value)) {
            model.add(resource, property, value);
        }
    }
    
    /**
     * Add a LocalDateTime value to a resource if not null.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param value The value to add
     */
    private void addDateTimeIfNotNull(Model model, Resource resource, Property property, LocalDateTime value) {
        if (value != null) {
            try {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
                cal.set(value.getYear(), value.getMonthValue() - 1, value.getDayOfMonth(), 
                        value.getHour(), value.getMinute(), value.getSecond());
                
                model.add(resource, property, model.createTypedLiteral(
                        DatatypeFactory.newInstance().newXMLGregorianCalendar(cal)));
            } catch (Exception e) {
                log.error("Error converting LocalDateTime to XSD DateTime", e);
                model.add(resource, property, value.toString());
            }
        }
    }
    
    /**
     * Add multiple string values to a resource.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param values The values to add
     */
    private void addStrings(Model model, Resource resource, Property property, List<String> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    model.add(resource, property, value);
                }
            }
        }
    }
    
    /**
     * Add multiple URI values to a resource.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param values The values to add
     */
    private void addURIs(Model model, Resource resource, Property property, List<URI> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            for (URI uri : values) {
                if (uri != null) {
                    model.add(resource, property, model.createResource(uri.toString()));
                }
            }
        }
    }
    
    /**
     * Add multiple string values as URIs to a resource.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param values The string values to add as URIs
     */
    private void addURIsFromStrings(Model model, Resource resource, Property property, List<String> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    model.add(resource, property, model.createResource(value));
                }
            }
        }
    }
    
    /**
     * Add an Agent object to a resource.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param agent The agent to add
     */
    private void addAgent(Model model, Resource resource, Property property, Agent agent) {
        if (agent != null) {
            // Create a submodel for the agent
            Resource agentResource = createResourceWithId(model, agent);
            
            // Add agent type
            if (StringUtils.isNotBlank(agent.getType())) {
                model.add(agentResource, RDF.type, model.createResource(agent.getType()));
            } else {
                model.add(agentResource, RDF.type, FOAF.Agent);
            }
            
            // Add basic properties
            addStringIfNotEmpty(model, agentResource, FOAF.name, agent.getName());
            addStringIfNotEmpty(model, agentResource, FOAF.mbox, agent.getEmail());
            
            // Add homepage
            if (agent.getHomepage() != null) {
                model.add(agentResource, FOAF.homepage, 
                        model.createResource(agent.getHomepage().toString()));
            }
            
            // Link the agent to the resource
            model.add(resource, property, agentResource);
        }
    }
    
    /**
     * Add a Location object to a resource.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param location The location to add
     */
    private void addLocation(Model model, Resource resource, Property property, Location location) {
        if (location != null) {
            // Create a submodel for the location
            Resource locationResource = createResourceWithId(model, location);
            
            // Add basic properties
            addStringIfNotEmpty(model, locationResource, FOAF.name, location.getName());
            
            // Add geometry properties if available
            addStringIfNotEmpty(model, locationResource, model.createProperty("http://www.w3.org/ns/locn#geometry"), 
                    location.getGeometry());
            addStringIfNotEmpty(model, locationResource, model.createProperty("http://www.w3.org/ns/locn#bbox"), 
                    location.getBbox());
            
            // Add lat/long if available
            if (location.getLatitude() != null && location.getLongitude() != null) {
                model.add(locationResource, model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat"),
                        model.createTypedLiteral(location.getLatitude().toString(), XSDDatatype.XSDdecimal));
                model.add(locationResource, model.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long"),
                        model.createTypedLiteral(location.getLongitude().toString(), XSDDatatype.XSDdecimal));
            }
            
            // Link the location to the resource
            model.add(resource, property, locationResource);
        }
    }
    
    /**
     * Add a PeriodOfTime object to a resource.
     * 
     * @param model The model to add to
     * @param resource The resource to add to
     * @param property The property to add
     * @param period The period to add
     */
    private void addPeriodOfTime(Model model, Resource resource, Property property, PeriodOfTime period) {
        if (period != null) {
            // Create a submodel for the period
            Resource periodResource = createResourceWithId(model, period);
            model.add(periodResource, RDF.type, DCTerms.PeriodOfTime);
            
            // Add start and end dates
            addDateTimeIfNotNull(model, periodResource, model.createProperty("http://www.w3.org/ns/dcat#startDate"), 
                    period.getStartDate());
            addDateTimeIfNotNull(model, periodResource, model.createProperty("http://www.w3.org/ns/dcat#endDate"), 
                    period.getEndDate());
            
            // Link the period to the resource
            model.add(resource, property, periodResource);
        }
    }
}
