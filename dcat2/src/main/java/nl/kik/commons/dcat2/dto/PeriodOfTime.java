package nl.kik.commons.dcat2.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a temporal period for a DCAT dataset.
 * 
 * As per DCAT2 specification, this is used to represent the temporal coverage of a dataset.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class PeriodOfTime extends DCATObject {
    
    /**
     * The start date of the period.
     */
    private LocalDateTime startDate;
    
    /**
     * The end date of the period.
     */
    private LocalDateTime endDate;
}
