package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Distribution extends DCATObject {
	String title;
	String description;
	ZonedDateTime issued;
	ZonedDateTime modified;
//	LicenseDocument license;
//	List<RightsStatement> accessRights;
//	List<RightsStatement> rights;
	// odrl:Policy hasPolicy;
	Set<URI> accessURL;
	Set<DataService> accessService;
	Set<URI> downloadURL;
	Double byteSize;
	Float spatialResolutionInMeters;
	Duration temporalResolution;
	Set<URI> conformsTo; // Standard
//	List<MediaType> mediaType;
//	List<MediaTypeOrExtent> format;
//	List<MediaType> packageFormat;
}
