package nl.kik.commons.dcat.model;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a DCAT2 Dataset.
 * 
 * In DCAT2, a Dataset is a collection of data, published or curated by a single source,
 * and available for access or download in one or more representations.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Dataset">DCAT2 Dataset</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Dataset extends Resource {
    
    /**
     * The distributions of this dataset.
     */
    private Set<Distribution> distributions = new HashSet<>();
    
    /**
     * The frequency at which the dataset is updated.
     */
    private Duration accrualPeriodicity;
    
    /**
     * The temporal coverage of the dataset.
     */
    private TemporalCoverage temporalCoverage;
    
    /**
     * The spatial coverage of the dataset.
     */
    private URI spatialCoverage;
    
    /**
     * The date when the dataset was issued.
     */
    private ZonedDateTime issued;
    
    /**
     * The date when the dataset was last modified.
     */
    private ZonedDateTime modified;
    
    /**
     * The minimum time period resolvable in the dataset.
     */
    private Duration temporalResolution;
    
    /**
     * The minimum spatial separation resolvable in the dataset, measured in meters.
     */
    private Double spatialResolutionInMeters;
    
    /**
     * Adds a distribution to the dataset.
     * 
     * @param distribution The distribution to add
     * @return This dataset for method chaining
     */
    public Dataset addDistribution(Distribution distribution) {
        if (distributions == null) {
            distributions = new HashSet<>();
        }
        distributions.add(distribution);
        return this;
    }
    
    /**
     * Represents the temporal coverage of a dataset.
     */
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    public static class TemporalCoverage {
        /**
         * The start date of the temporal coverage.
         */
        private ZonedDateTime startDate;
        
        /**
         * The end date of the temporal coverage.
         */
        private ZonedDateTime endDate;
        
        /**
         * Creates a new temporal coverage with the given start and end dates.
         * 
         * @param startDate The start date
         * @param endDate The end date
         */
        public TemporalCoverage(ZonedDateTime startDate, ZonedDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}