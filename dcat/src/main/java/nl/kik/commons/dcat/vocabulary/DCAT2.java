package nl.kik.commons.dcat.vocabulary;

/**
 * Vocabulary class for DCAT2 (Data Catalog Vocabulary).
 * 
 * This class defines constants for the DCAT2 vocabulary terms.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/">DCAT2 Specification</a>
 */
public final class DCAT2 {
    /** The DCAT2 namespace: http://www.w3.org/ns/dcat# */
    public static final String NS = "http://www.w3.org/ns/dcat#";

    // Classes
    /** Catalog - A curated collection of metadata about resources (e.g., datasets and data services in the context of a data catalog). */
    public static final String CATALOG = NS + "Catalog";
    
    /** Dataset - A collection of data, published or curated by a single source, and available for access or download in one or more representations. */
    public static final String DATASET = NS + "Dataset";
    
    /** Distribution - A specific representation of a dataset. A dataset might be available in multiple serializations that may differ in various ways, including natural language, media-type or format, schematic organization, temporal and spatial resolution, level of detail or profiles (which might specify any or all of the above). */
    public static final String DISTRIBUTION = NS + "Distribution";
    
    /** DataService - A collection of operations that provides access to one or more datasets or data processing functions. */
    public static final String DATA_SERVICE = NS + "DataService";
    
    /** CatalogRecord - A record in a data catalog, describing a single dataset or data service. */
    public static final String CATALOG_RECORD = NS + "CatalogRecord";
    
    /** Resource - An entity described by metadata in the catalog. This is a super-class of dcat:Dataset, dcat:DataService, etc. */
    public static final String RESOURCE = NS + "Resource";

    // Properties
    /** accessURL - A URL of a resource that gives access to a distribution of the dataset. */
    public static final String ACCESS_URL = NS + "accessURL";
    
    /** downloadURL - A URL that is a direct link to a downloadable file in a given format. */
    public static final String DOWNLOAD_URL = NS + "downloadURL";
    
    /** landingPage - A web page that can be navigated to in a web browser to gain access to the catalog, a dataset, its distributions and/or additional information. */
    public static final String LANDING_PAGE = NS + "landingPage";
    
    /** contactPoint - Relevant contact information for the cataloged resource. */
    public static final String CONTACT_POINT = NS + "contactPoint";
    
    /** keyword - A keyword or tag describing a resource. */
    public static final String KEYWORD = NS + "keyword";
    
    /** theme - A main category of the resource. A resource can have multiple themes. */
    public static final String THEME = NS + "theme";
    
    /** dataset - A collection of data that is listed in the catalog. */
    public static final String DATASET_PROPERTY = NS + "dataset";
    
    /** distribution - An available distribution of the dataset. */
    public static final String DISTRIBUTION_PROPERTY = NS + "distribution";
    
    /** service - A site or endpoint that is listed in the catalog. */
    public static final String SERVICE = NS + "service";
    
    /** catalog - A catalog whose contents are of interest in the context of this catalog. */
    public static final String CATALOG_PROPERTY = NS + "catalog";
    
    /** record - A record describing the registration of a single resource. */
    public static final String RECORD = NS + "record";
    
    /** servesDataset - A collection of data that this DataService can distribute. */
    public static final String SERVES_DATASET = NS + "servesDataset";
    
    /** endpointURL - The root location or primary endpoint of the service (an IRI). */
    public static final String ENDPOINT_URL = NS + "endpointURL";
    
    /** endpointDescription - A description of the services available via the end-points, including their operations, parameters etc. */
    public static final String ENDPOINT_DESCRIPTION = NS + "endpointDescription";
    
    /** mediaType - The media type of the distribution as defined by IANA. */
    public static final String MEDIA_TYPE = NS + "mediaType";
    
    /** format - The file format of the distribution. */
    public static final String FORMAT = NS + "format";
    
    /** byteSize - The size of a distribution in bytes. */
    public static final String BYTE_SIZE = NS + "byteSize";
    
    /** temporalResolution - Minimum time period resolvable in the dataset. */
    public static final String TEMPORAL_RESOLUTION = NS + "temporalResolution";
    
    /** spatialResolutionInMeters - Minimum spatial separation resolvable in a dataset, measured in meters. */
    public static final String SPATIAL_RESOLUTION_IN_METERS = NS + "spatialResolutionInMeters";
    
    /** compressFormat - The compression format of the distribution in which the data is contained in a compressed form. */
    public static final String COMPRESS_FORMAT = NS + "compressFormat";
    
    /** packageFormat - The package format of the distribution in which one or more data files are grouped together. */
    public static final String PACKAGE_FORMAT = NS + "packageFormat";

    // Private constructor to prevent instantiation
    private DCAT2() {
    }
}