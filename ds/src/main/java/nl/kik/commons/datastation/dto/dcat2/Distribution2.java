package nl.kik.commons.datastation.dto.dcat2;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.dcat.Distribution;

/**
 * Represents a distribution in DCAT2.
 * Extends the existing Distribution class to add DCAT2-specific properties.
 * 
 * A distribution represents a specific available form of a dataset. 
 * Each dataset might be available in different forms, these forms might represent different formats 
 * of the dataset or different endpoints. Examples of distributions include a downloadable CSV file, 
 * an API or an RSS feed.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Distribution2 extends Distribution {
    
    /**
     * The compression format of the distribution.
     */
    private URI compressionFormat;
    
    /**
     * The packaging format of the distribution.
     */
    private URI packageFormat;
    
    /**
     * The temporal properties of the distribution.
     */
    private ResourceTimeEvolution timeEvolution;
    
    /**
     * The qualified relations to other resources.
     */
    private Set<Relationship2> qualifiedRelation;
    
    /**
     * The access services for this distribution.
     */
    private Set<DataService2> accessService2;
}
