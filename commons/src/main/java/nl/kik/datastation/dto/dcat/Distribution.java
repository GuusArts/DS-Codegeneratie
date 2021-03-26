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
public class Distribution {
	String title;
	String description;
	ZonedDateTime issued;
	ZonedDateTime modified;
	LicenseDocument license;
	List<RightsStatement> accessRights;
	List<RightsStatement> rights;
	// odrl:Policy hasPolicy;
	URL accessURL;
	DataService accessService;
	URL downloadURL;
	Double byteSize;
	Float spatialResolutionInMeters;
	Duration temporalResolution;
	Standard conformsTo;
	MediaType mediaType;
	MediaTypeOrExtent format;
	MediaType packageFormat;
}
