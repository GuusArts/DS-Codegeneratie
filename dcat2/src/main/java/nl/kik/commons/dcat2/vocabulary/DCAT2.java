package nl.kik.commons.dcat2.vocabulary;

/**
 * DCAT2 Vocabulary definitions.
 * This class defines all the RDF terms used in DCAT2.
 * Based on W3C DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/
 */
public final class DCAT2 {

    /**
     * The DCAT2 namespace: https://www.w3.org/ns/dcat#
     */
    public static final String NS = "https://www.w3.org/ns/dcat#";

    /* Classes */

    /**
     * A collection of data, published or curated by a single agent.
     */
    public static final String CATALOG = NS + "Catalog";
    
    /**
     * A set of related datasets or data services that are published together.
     */
    public static final String CATALOG_RECORD = NS + "CatalogRecord";
    
    /**
     * A collection of data, published or curated by a single agent.
     */
    public static final String DATASET = NS + "Dataset";
    
    /**
     * A site or end-point that provides access to datasets or data processing capabilities.
     */
    public static final String DATA_SERVICE = NS + "DataService";
    
    /**
     * A web-accessible resource that may contain data.
     */
    public static final String DISTRIBUTION = NS + "Distribution";
    
    /**
     * A conceptual entity that represents the information published.
     */
    public static final String RESOURCE = NS + "Resource";
    
    /**
     * A collection of related resources, including datasets and data services.
     */
    public static final String RELATIONSHIP = NS + "Relationship";
    
    /**
     * A role that may be played by an entity in a given context.
     */
    public static final String ROLE = NS + "Role";
    
    /* Properties */
    
    /**
     * A resource that is accessed by the service.
     */
    public static final String ACCESS_SERVICE = NS + "accessService";
    
    /**
     * A URL of a resource that gives access to a distribution of the dataset.
     */
    public static final String ACCESS_URL = NS + "accessURL";
    
    /**
     * The root location or primary endpoint of a service.
     */
    public static final String ENDPOINT_URL = NS + "endpointURL";
    
    /**
     * A description of the service or dataset.
     */
    public static final String DESCRIPTION = NS + "description";
    
    /**
     * A catalogue that contains this dataset.
     */
    public static final String IN_CATALOG = NS + "inCatalog";
    
    /**
     * A dataset that is part of the collection.
     */
    public static final String DATASET_PROP = NS + "dataset";
    
    /**
     * A distribution of the dataset.
     */
    public static final String DISTRIBUTION_PROP = NS + "distribution";
    
    /**
     * A landing page, feed, SPARQL endpoint or other type of resource that
     * provides access to the catalog.
     */
    public static final String ENDPOINT = NS + "endpoint";
    
    /**
     * A web service that provides the catalog.
     */
    public static final String SERVICE = NS + "service";
    
    /**
     * The URL of a downloadable file in a given format.
     */
    public static final String DOWNLOAD_URL = NS + "downloadURL";
    
    /**
     * The frequency at which the dataset is published.
     */
    public static final String FREQUENCY = NS + "frequency";
    
    /**
     * A keyword or tag describing the dataset.
     */
    public static final String KEYWORD = NS + "keyword";
    
    /**
     * A Web page that provides information about the dataset.
     */
    public static final String LANDING_PAGE = NS + "landingPage";
    
    /**
     * A media type of the distribution.
     */
    public static final String MEDIA_TYPE = NS + "mediaType";
    
    /**
     * The frequency at which the dataset is updated.
     */
    public static final String PUBLISHER = NS + "publisher";
    
    /**
     * A related resource.
     */
    public static final String HAS_PART = NS + "hasPart";
    
    /**
     * A related resource in which the described resource is physically or
     * logically included.
     */
    public static final String IS_PART_OF = NS + "isPartOf";
    
    /**
     * The size of a distribution in bytes.
     */
    public static final String BYTE_SIZE = NS + "byteSize";
    
    /**
     * The main identifier for the dataset.
     */
    public static final String IDENTIFIER = NS + "identifier";
    
    /**
     * A name given to the catalog.
     */
    public static final String TITLE = NS + "title";
    
    /**
     * A unique identifier of the resource.
     */
    public static final String QUALIFIED_RELATION = NS + "qualifiedRelation";
    
    /**
     * The license under which the distribution is made available.
     */
    public static final String LICENSE = NS + "license";
    
    /**
     * The entity responsible for making the dataset available.
     */
    public static final String RELEASE_DATE = NS + "releaseDate";
    
    /**
     * The date of formal issuance (e.g., publication) of the dataset.
     */
    public static final String ISSUED = NS + "issued";
    
    /**
     * Most recent date on which the dataset was changed, updated or modified.
     */
    public static final String MODIFIED = NS + "modified";
    
    /**
     * The language of the dataset.
     */
    public static final String LANGUAGE = NS + "language";
    
    /**
     * The spatial coverage of the dataset.
     */
    public static final String SPATIAL = NS + "spatial";
    
    /**
     * The temporal period that the dataset covers.
     */
    public static final String TEMPORAL = NS + "temporal";
    
    /**
     * A version number or other version designation of the dataset.
     */
    public static final String VERSION = NS + "version";
    
    /**
     * A URL that provides a formal machine-readable description of the service.
     */
    public static final String SERVES_DATASET = NS + "servesDataset";
    
    /**
     * A concept scheme that defines the themes or categories used to classify the catalog.
     */
    public static final String THEME_TAXONOMY = NS + "themeTaxonomy";
    
    /**
     * A data format of the distribution.
     */
    public static final String FORMAT = NS + "format";
    
    /**
     * The compression format of the distribution.
     */
    public static final String COMPRESSION_FORMAT = NS + "compressFormat";
    
    /**
     * The packaging format of the distribution.
     */
    public static final String PACKAGING_FORMAT = NS + "packageFormat";
    
    /**
     * This links to the temporal coverage of the dataset.
     */
    public static final String TEMPORAL_RESOLUTION = NS + "temporalResolution";
    
    /**
     * This links to the spatial coverage of the dataset.
     */
    public static final String SPATIAL_RESOLUTION_IN_METERS = NS + "spatialResolutionInMeters";
    
    /**
     * The entity who primarily produces the resource.
     */
    public static final String CREATOR = NS + "creator";
    
    private DCAT2() {
        // Static class, no instances
    }
}
