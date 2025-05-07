package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * A web service that provides access to data.
 * Based on DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Data_Service
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DataService extends Resource {
    // From existing implementation
    URI endpointURL;
    Set<RDFObject> endpointDescription;
    Set<Dataset> servesDataset;
    
    // New DCAT2 properties
    Set<DatasetSeries> servesDatasetSeries;
    
    // Service specifications
    String serviceType;  // The type of service (e.g., "REST API", "SPARQL endpoint")
    Set<URI> conformsTo; // Standards the service conforms to
}
