package nl.kik.commons.datastation.dto.dcat.vocabulary;

/**
 * Vocabulary class for DCAT2 terms.
 * Based on the W3C DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/
 */
public final class DCAT2Vocabulary {
    // DCAT2 namespace
    public static final String DCAT_NAMESPACE = "http://www.w3.org/ns/dcat#";
    
    // Classes
    public static final String CATALOG = DCAT_NAMESPACE + "Catalog";
    public static final String CATALOG_RECORD = DCAT_NAMESPACE + "CatalogRecord";
    public static final String DATASET = DCAT_NAMESPACE + "Dataset";
    public static final String DATASET_SERIES = DCAT_NAMESPACE + "DatasetSeries";
    public static final String DATA_SERVICE = DCAT_NAMESPACE + "DataService";
    public static final String DISTRIBUTION = DCAT_NAMESPACE + "Distribution";
    public static final String RELATIONSHIP = DCAT_NAMESPACE + "Relationship";
    public static final String RESOURCE = DCAT_NAMESPACE + "Resource";
    public static final String ROLE = DCAT_NAMESPACE + "Role";
    
    // Properties
    public static final String ACCESS_SERVICE = DCAT_NAMESPACE + "accessService";
    public static final String ACCESS_URL = DCAT_NAMESPACE + "accessURL";
    public static final String BBOX = DCAT_NAMESPACE + "bbox";
    public static final String CATALOG_PROP = DCAT_NAMESPACE + "catalog";
    public static final String CENTROID = DCAT_NAMESPACE + "centroid";
    public static final String CONTACT_POINT = DCAT_NAMESPACE + "contactPoint";
    public static final String DATASET_PROP = DCAT_NAMESPACE + "dataset";
    public static final String DATA_SERVICE_PROP = DCAT_NAMESPACE + "dataService";
    public static final String DISTRIBUTION_PROP = DCAT_NAMESPACE + "distribution";
    public static final String DOWNLOAD_URL = DCAT_NAMESPACE + "downloadURL";
    public static final String ENDPOINT_DESCRIPTION = DCAT_NAMESPACE + "endpointDescription";
    public static final String ENDPOINT_URL = DCAT_NAMESPACE + "endpointURL";
    public static final String HAS_POLICY = DCAT_NAMESPACE + "hasPolicy";
    public static final String KEYWORD = DCAT_NAMESPACE + "keyword";
    public static final String LANDING_PAGE = DCAT_NAMESPACE + "landingPage";
    public static final String MEDIA_TYPE = DCAT_NAMESPACE + "mediaType";
    public static final String PACKAGE_FORMAT = DCAT_NAMESPACE + "packageFormat";
    public static final String QUALIFIED_ATTRIBUTION = DCAT_NAMESPACE + "qualifiedAttribution";
    public static final String QUALIFIED_RELATION = DCAT_NAMESPACE + "qualifiedRelation";
    public static final String RECORD = DCAT_NAMESPACE + "record";
    public static final String SERVICE_PROP = DCAT_NAMESPACE + "service";
    public static final String SERVES_DATASET = DCAT_NAMESPACE + "servesDataset";
    public static final String IN_SERIES = DCAT_NAMESPACE + "inSeries";
    public static final String SPATIAL_RESOLUTION_IN_METERS = DCAT_NAMESPACE + "spatialResolutionInMeters";
    public static final String TEMPORAL_RESOLUTION = DCAT_NAMESPACE + "temporalResolution";
    public static final String THEME_TAXONOMY = DCAT_NAMESPACE + "themeTaxonomy";
    public static final String HAD_ROLE = DCAT_NAMESPACE + "hadRole";
    
    // New in DCAT2
    public static final String START_DATE = DCAT_NAMESPACE + "startDate";
    public static final String END_DATE = DCAT_NAMESPACE + "endDate";
    public static final String HAS_VERSION = DCAT_NAMESPACE + "hasVersion";
    public static final String IS_VERSION_OF = DCAT_NAMESPACE + "isVersionOf";
    public static final String PREVIOUS_VERSION = DCAT_NAMESPACE + "previousVersion";
    public static final String VERSION = DCAT_NAMESPACE + "version";
    public static final String VERSION_NOTES = DCAT_NAMESPACE + "versionNotes";
    public static final String BYTE_SIZE = DCAT_NAMESPACE + "byteSize";
    public static final String COMPRESSION_FORMAT = DCAT_NAMESPACE + "compressFormat";
    
    // Private constructor to prevent instantiation
    private DCAT2Vocabulary() {
        // This is a utility class not meant to be instantiated
    }
}