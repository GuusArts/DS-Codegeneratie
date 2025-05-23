package nl.kik.commons.dcat.vocabulary;

/**
 * Vocabulary constants for DCAT2 (Data Catalog Vocabulary).
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/">DCAT2 Specification</a>
 */
public final class DCAT2 {

    private DCAT2() {
        // Private constructor to prevent instantiation
    }

    /** The DCAT namespace: http://www.w3.org/ns/dcat# */
    public static final String NS = "http://www.w3.org/ns/dcat#";

    // Classes
    /** dcat:Catalog */
    public static final String CATALOG = NS + "Catalog";
    /** dcat:Dataset */
    public static final String DATASET = NS + "Dataset";
    /** dcat:Distribution */
    public static final String DISTRIBUTION = NS + "Distribution";
    /** dcat:DataService */
    public static final String DATA_SERVICE = NS + "DataService";
    /** dcat:CatalogRecord */
    public static final String CATALOG_RECORD = NS + "CatalogRecord";
    /** dcat:Resource */
    public static final String RESOURCE = NS + "Resource";

    // Properties
    /** dcat:dataset */
    public static final String DATASET_PROPERTY = NS + "dataset";
    /** dcat:distribution */
    public static final String DISTRIBUTION_PROPERTY = NS + "distribution";
    /** dcat:service */
    public static final String SERVICE_PROPERTY = NS + "service";
    /** dcat:record */
    public static final String RECORD_PROPERTY = NS + "record";
    /** dcat:themeTaxonomy */
    public static final String THEME_TAXONOMY = NS + "themeTaxonomy";
    /** dcat:accessURL */
    public static final String ACCESS_URL = NS + "accessURL";
    /** dcat:downloadURL */
    public static final String DOWNLOAD_URL = NS + "downloadURL";
    /** dcat:landingPage */
    public static final String LANDING_PAGE = NS + "landingPage";
    /** dcat:contactPoint */
    public static final String CONTACT_POINT = NS + "contactPoint";
    /** dcat:keyword */
    public static final String KEYWORD = NS + "keyword";
    /** dcat:theme */
    public static final String THEME = NS + "theme";
    /** dcat:catalog */
    public static final String CATALOG_PROPERTY = NS + "catalog";
    /** dcat:servesDataset */
    public static final String SERVES_DATASET = NS + "servesDataset";
    /** dcat:endpointURL */
    public static final String ENDPOINT_URL = NS + "endpointURL";
    /** dcat:endpointDescription */
    public static final String ENDPOINT_DESCRIPTION = NS + "endpointDescription";
    /** dcat:mediaType */
    public static final String MEDIA_TYPE = NS + "mediaType";
    /** dcat:format */
    public static final String FORMAT = NS + "format";
    /** dcat:byteSize */
    public static final String BYTE_SIZE = NS + "byteSize";
    /** dcat:spatialResolutionInMeters */
    public static final String SPATIAL_RESOLUTION_IN_METERS = NS + "spatialResolutionInMeters";
    /** dcat:temporalResolution */
    public static final String TEMPORAL_RESOLUTION = NS + "temporalResolution";
    /** dcat:compressFormat */
    public static final String COMPRESS_FORMAT = NS + "compressFormat";
    /** dcat:packageFormat */
    public static final String PACKAGE_FORMAT = NS + "packageFormat";
}