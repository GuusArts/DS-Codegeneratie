package nl.kik.commons.datastation.dto.dcat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A spatial region or named place.
 * Based on DCAT2 specification and DCTerms Location.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Location extends DCATObject {
    // Bounding box in WKT format
    String bbox;
    
    // Centroid in WKT format
    String centroid;
    
    // Human readable name of the location
    String name;
    
    // Geometry in GeoJSON format
    String geometry;
}