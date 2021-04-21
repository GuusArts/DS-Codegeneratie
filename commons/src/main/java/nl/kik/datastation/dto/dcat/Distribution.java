package nl.kik.datastation.dto.dcat;

import java.net.URL;
import java.time.Duration;
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
public class Distribution extends DCATObject {
	String title;
	String description;
	ZonedDateTime issued;
	ZonedDateTime modified;
//	LicenseDocument license;
//	List<RightsStatement> accessRights;
//	List<RightsStatement> rights;
	// odrl:Policy hasPolicy;
	List<URL> accessURL;
	List<DataService> accessService;
	List<URL> downloadURL;
	Double byteSize;
	Float spatialResolutionInMeters;
	Duration temporalResolution;
	URL conformsTo;// Standard
//	List<MediaType> mediaType;
//	List<MediaTypeOrExtent> format;
//	List<MediaType> packageFormat;
}
