package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.foaf.Agent;
import nl.kik.commons.dto.RDFObject;

/**
 * A resource published or curated by a single agent.
 * Based on DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Resource
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Resource extends DCATObject {
    // Properties from existing implementation
    Set<URI> accessRights;
    Set<URI> conformsTo;
    Kind contactPoint;
    Agent creator;
    String description;
    String title;
    ZonedDateTime issued;
    ZonedDateTime modified;
    Set<String> language;
    Agent publisher;
    String identifier;
    Set<RDFObject> relation;
    Set<Relationship> qualifiedRelation;
    Set<String> keyword;
    URI landingPage;
    Set<Relationship> qualifiedAttribution;
    URI license;
    Set<URI> rights;
    Set<URI> hasPolicy;
    Set<RDFObject> isReferencedBy;
    
    // Enhanced DCAT2 properties
    Set<Resource> isPartOf;  // Indicates a resource of which this resource is a part
    Set<Resource> hasPart;   // Indicates a related resource that is included in the described resource
    URI type;                // The nature or genre of the resource
    Set<URI> theme;          // A category of the resource
    
    // Additional properties for extensions
    ZonedDateTime created;   // Date of creation
    String source;           // A reference to a resource from which the present resource is derived
    Set<Agent> contributor;  // An entity responsible for making contributions to the resource
    Set<URI> hasVersion;     // A related resource that is a version, edition, or adaptation of the described resource
    Set<URI> isVersionOf;    // A related resource of which the described resource is a version, edition, or adaptation
    String versionInfo;      // Version information
}
