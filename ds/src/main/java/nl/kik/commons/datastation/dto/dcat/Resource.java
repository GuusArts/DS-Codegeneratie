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

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Resource extends DCATObject {
//	List<RightsStatement> accessRights;
	Set<URI> conformsTo; // Standard
	Kind contactPoint;
	Agent creator;
	String description;
	String title;
	ZonedDateTime issued;
	ZonedDateTime modified;
//	dct:LinguisticSystem language;
	Agent publisher;
	String identifier;
//	skos:Concept;
//	rdfs:Class type;
	Set<RDFObject> relation;
	Set<Relationship> qualifiedRelation;
	Set<String> keyword;
//	foaf:Document landingPage;
//	prov:Attribution qualifiedAttribution;
	URI license; // LicenseDocument
//	List<RightsStatement> rights;
//	odrl:Policy;
	Set<RDFObject> isReferencedBy;
}
