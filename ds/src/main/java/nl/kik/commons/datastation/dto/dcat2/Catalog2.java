package nl.kik.commons.datastation.dto.dcat2;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.dcat.Catalog;

/**
 * Represents a catalog in DCAT2.
 * Extends the existing Catalog class to add DCAT2-specific properties.
 * 
 * A catalog is a curated collection of metadata about resources (e.g., datasets and data services).
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Catalog2 extends Catalog {
    
    /**
     * The dataset series in this catalog.
     */
    private Set<DatasetSeries> series;
    
    /**
     * The temporal properties of the catalog.
     */
    private ResourceTimeEvolution timeEvolution;
    
    /**
     * The qualified relations to other resources.
     */
    private Set<Relationship2> qualifiedRelation;
    
    /**
     * The services in this catalog.
     */
    private Set<DataService2> service2;
    
    /**
     * The datasets in this catalog.
     */
    private Set<Dataset2> dataset2;
}
