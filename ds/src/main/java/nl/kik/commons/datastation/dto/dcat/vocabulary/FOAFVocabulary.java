package nl.kik.commons.datastation.dto.dcat.vocabulary;

/**
 * Vocabulary class for Friend of a Friend (FOAF).
 * Based on http://xmlns.com/foaf/spec/
 */
public final class FOAFVocabulary {
    // FOAF namespace
    public static final String FOAF_NAMESPACE = "http://xmlns.com/foaf/0.1/";
    
    // Classes
    public static final String AGENT = FOAF_NAMESPACE + "Agent";
    public static final String DOCUMENT = FOAF_NAMESPACE + "Document";
    public static final String GROUP = FOAF_NAMESPACE + "Group";
    public static final String ORGANIZATION = FOAF_NAMESPACE + "Organization";
    public static final String PERSON = FOAF_NAMESPACE + "Person";
    
    // Properties
    public static final String HOMEPAGE = FOAF_NAMESPACE + "homepage";
    public static final String MEMBER = FOAF_NAMESPACE + "member";
    public static final String NAME = FOAF_NAMESPACE + "name";
    public static final String PAGE = FOAF_NAMESPACE + "page";
    public static final String PRIMARY_TOPIC = FOAF_NAMESPACE + "primaryTopic";
    
    // Private constructor to prevent instantiation
    private FOAFVocabulary() {
        // This is a utility class not meant to be instantiated
    }
}