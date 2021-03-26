package nl.kik.datastation.dto.dcat;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class Resource {
	List<RightsStatement> accessRights;
	Standard conformsTo;
	Kind contactPoint;
//	foaf:Agent creator;
	String description;
	String title;
	ZonedDateTime issued;
	ZonedDateTime modified;
	String language;
//	foaf:Agent publisher;
	String identifier;
//	skos:Concept;
//	rdfs:Class type;
	List<Object> relation;
	List<Relationship> qualifiedInfluence;
	List<String> keyword;
//	foaf:Document landingPage;
//	prov:Attribution qalifiedAttribution;
	LicenseDocument license;
	List<RightsStatement> rights;
//	odrl:Policy;
	List<Object> isReferencedBy;
}
