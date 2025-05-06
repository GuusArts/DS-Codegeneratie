package nl.kik.commons.datastation.dto.dcat2;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.dcat.Dataset;

/**
 * Represents a dataset in DCAT2.
 * Extends the existing Dataset class to add DCAT2-specific properties.
 * 
 * A dataset is a collection of data, published or curated by a single agent, 
 * and available for access or download in one or more representations.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Dataset2 extends Dataset {
    
    /**
     * The qualified relation to other resources.
     */
    private Set<Relationship2> qualifiedRelation;
    
    /**
     * The temporal properties of the dataset.
     */
    private ResourceTimeEvolution timeEvolution;
    
    /**
     * The dataset series this dataset is part of.
     */
    private Set<DatasetSeries> inSeries;
    
    /**
     * The previous version of this dataset.
     */
    private Dataset2 previousVersion;
    
    /**
     * The next version of this dataset.
     */
    private Dataset2 nextVersion;
    
    /**
     * The version info of this dataset.
     */
    private String versionInfo;
    
    /**
     * The version notes of this dataset.
     */
    private String versionNotes;
    
    /**
     * The provenance of this dataset.
     */
    private URI provenance;
}
