package nl.kik.commons.dcat.model;

import java.net.URI;
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
 * Represents a DCAT2 DataService.
 * 
 * In DCAT2, a DataService is a collection of operations that provides access to one or more datasets
 * or data processing functions.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Data_Service">DCAT2 DataService</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DataService extends Resource {
    
    /**
     * The root location or primary endpoint of the service (an IRI).
     */
    private URI endpointURL;
    
    /**
     * A description of the services available via the end-points, including their operations, parameters etc.
     */
    private URI endpointDescription;
    
    /**
     * The datasets that this service can distribute.
     */
    private Set<Dataset> servesDatasets = new HashSet<>();
    
    /**
     * The date when the service was issued.
     */
    private ZonedDateTime issued;
    
    /**
     * The date when the service was last modified.
     */
    private ZonedDateTime modified;
    
    /**
     * The license of the service.
     */
    private URI license;
    
    /**
     * Adds a dataset that this service can distribute.
     * 
     * @param dataset The dataset to add
     * @return This service for method chaining
     */
    public DataService addServesDataset(Dataset dataset) {
        if (servesDatasets == null) {
            servesDatasets = new HashSet<>();
        }
        servesDatasets.add(dataset);
        return this;
    }
}