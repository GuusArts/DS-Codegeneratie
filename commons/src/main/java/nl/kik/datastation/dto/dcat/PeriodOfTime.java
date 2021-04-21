package nl.kik.datastation.dto.dcat;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class PeriodOfTime extends DCATObject {
	ZonedDateTime startDate;
	ZonedDateTime endDate;
//	time:Instance hasBeginning;
//	time:Instance hasEnd;
}
