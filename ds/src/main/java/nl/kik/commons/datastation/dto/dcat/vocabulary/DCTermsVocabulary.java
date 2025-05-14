package nl.kik.commons.datastation.dto.dcat.vocabulary;

/**
 * Vocabulary class for Dublin Core Terms.
 * Based on https://www.dublincore.org/specifications/dublin-core/dcmi-terms/
 */
public final class DCTermsVocabulary {
    // DC Terms namespace
    public static final String DCT_NAMESPACE = "http://purl.org/dc/terms/";
    
    // Classes
    public static final String LICENSE_DOCUMENT = DCT_NAMESPACE + "LicenseDocument";
    public static final String LOCATION = DCT_NAMESPACE + "Location";
    public static final String PERIOD_OF_TIME = DCT_NAMESPACE + "PeriodOfTime";
    public static final String RIGHTS_STATEMENT = DCT_NAMESPACE + "RightsStatement";
    
    // Properties
    public static final String ACCESS_RIGHTS = DCT_NAMESPACE + "accessRights";
    public static final String ACCRUAL_PERIODICITY = DCT_NAMESPACE + "accrualPeriodicity";
    public static final String CONFORMS_TO = DCT_NAMESPACE + "conformsTo";
    public static final String CREATED = DCT_NAMESPACE + "created";
    public static final String CREATOR = DCT_NAMESPACE + "creator";
    public static final String DESCRIPTION = DCT_NAMESPACE + "description";
    public static final String FORMAT = DCT_NAMESPACE + "format";
    public static final String HAS_PART = DCT_NAMESPACE + "hasPart";
    public static final String HAS_VERSION = DCT_NAMESPACE + "hasVersion";
    public static final String IDENTIFIER = DCT_NAMESPACE + "identifier";
    public static final String IS_PART_OF = DCT_NAMESPACE + "isPartOf";
    public static final String IS_REFERENCED_BY = DCT_NAMESPACE + "isReferencedBy";
    public static final String IS_VERSION_OF = DCT_NAMESPACE + "isVersionOf";
    public static final String ISSUED = DCT_NAMESPACE + "issued";
    public static final String LANGUAGE = DCT_NAMESPACE + "language";
    public static final String LICENSE = DCT_NAMESPACE + "license";
    public static final String MODIFIED = DCT_NAMESPACE + "modified";
    public static final String PUBLISHER = DCT_NAMESPACE + "publisher";
    public static final String RELATION = DCT_NAMESPACE + "relation";
    public static final String RIGHTS = DCT_NAMESPACE + "rights";
    public static final String SOURCE = DCT_NAMESPACE + "source";
    public static final String SPATIAL = DCT_NAMESPACE + "spatial";
    public static final String SUBJECT = DCT_NAMESPACE + "subject";
    public static final String TEMPORAL = DCT_NAMESPACE + "temporal";
    public static final String TITLE = DCT_NAMESPACE + "title";
    public static final String TYPE = DCT_NAMESPACE + "type";
    
    // Private constructor to prevent instantiation
    private DCTermsVocabulary() {
        // This is a utility class not meant to be instantiated
    }
}