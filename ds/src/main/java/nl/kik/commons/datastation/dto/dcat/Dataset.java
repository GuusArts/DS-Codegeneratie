package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.Duration;
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
public class Dataset extends Resource {
	Set<Distribution> distribution;
	URI accrualPeriodicity; // Frequency
//	Location spatial;
	Float spatialResolutionInMeters;
	PeriodOfTime temporal;
	Duration temporalResolution;
//	prov:Activity wasGeneratedBy;
}
