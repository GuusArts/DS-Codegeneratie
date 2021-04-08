package nl.kik.datastation.dto.dcat;

import java.net.URL;
import java.time.Duration;
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
public class Dataset extends Resource {
	List<Distribution> distribution;
	URL accrualPeriodicity; // Frequency
	Location spatial;
	Float spatialResolutionInMeters;
	PeriodOfTime temporal;
	Duration temporalResolution;
//	prov:Activity wasGeneratedBy;
}
