package nl.kik.commons.dcat2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a spatial location for a DCAT dataset.
 * 
 * As per DCAT2 specification, this is used to represent the spatial coverage of a dataset.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Location extends DCATObject {
    
    /**
     * The geographic name of the location.
     */
    private String name;
    
    /**
     * The longitude coordinate.
     */
    private Double longitude;
    
    /**
     * The latitude coordinate.
     */
    private Double latitude;
    
    /**
     * The geometry as WKT (Well-known text) string.
     */
    private String geometry;
    
    /**
     * The bounding box as a WKT string.
     */
    private String bbox;
    
    /**
     * The centroid as a WKT string.
     */
    private String centroid;
}
