package nl.kik.commons.dcat2.dto;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A DCAT DataService.
 * 
 * A data service is a collection of operations that provide access to one or more datasets 
 * or data processing functions.
 * 
 * As per DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Data_Service
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DataService extends DCATObject {
    
    /**
     * The title of the data service.
     */
    private String title;
    
    /**
     * A description of the data service.
     */
    private String description;
    
    /**
     * A URL that provides the base address or primary endpoint of the service.
     */
    private URI endpointURL;
    
    /**
     * The standard or specification for the API of the service.
     */
    private URI endpointDescription;
    
    /**
     * A collection of operations that make up this service.
     */
    private List<URI> servesDataset;
    
    /**
     * The license under which the service is made available.
     */
    private URI license;
    
    /**
     * Access rights expressed as a URI to an access rights statement.
     */
    private URI accessRights;
}
