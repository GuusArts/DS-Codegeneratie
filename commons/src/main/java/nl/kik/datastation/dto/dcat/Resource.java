package nl.kik.datastation.dto.dcat;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.datastation.dto.foaf.Agent;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class Resource extends DCATObject {
//	List<RightsStatement> accessRights;
	URL conformsTo; // Standard
	Kind contactPoint;
	Agent creator;
	String description;
	String title;
	ZonedDateTime issued;
	ZonedDateTime modified;
	String language;
	Agent publisher;
	String identifier;
//	skos:Concept;
//	rdfs:Class type;
	List<Object> relation;
	List<Relationship> qualifiedInfluence;
	List<String> keyword;
//	foaf:Document landingPage;
//	prov:Attribution qualifiedAttribution;
	URL license; // LicenseDocument
//	List<RightsStatement> rights;
//	odrl:Policy;
	List<Object> isReferencedBy;
}
