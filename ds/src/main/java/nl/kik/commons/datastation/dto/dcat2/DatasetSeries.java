package nl.kik.commons.datastation.dto.dcat2;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.dcat.Dataset;

/**
 * Represents a collection of datasets that share common characteristics.
 * This is a new class introduced in DCAT2.
 * 
 * A dataset series is a collection of datasets that:
 * - are published separately
 * - share common characteristics that group them
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DatasetSeries extends Dataset {
    
    /**
     * The datasets that are part of this series.
     */
    private Set<Dataset> seriesMember;
    
    /**
     * Temporal properties of the dataset series as a whole.
     */
    private ResourceTimeEvolution temporalResolution;
}
