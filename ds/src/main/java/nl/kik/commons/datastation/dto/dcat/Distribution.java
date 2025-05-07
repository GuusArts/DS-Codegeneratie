package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A specific representation of a dataset.
 * A dataset might be available in multiple serializations that may differ in format, schemaVersion, etc.
 * Based on DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Distribution
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Distribution extends DCATObject {
    // Properties from existing implementation
    String title;
    String description;
    ZonedDateTime issued;
    ZonedDateTime modified;
    URI license;
    Set<URI> accessRights;
    Set<URI> rights;
    Set<URI> accessURL;
    Set<DataService> accessService;
    Set<URI> downloadURL;
    Double byteSize;
    Float spatialResolutionInMeters;
    Duration temporalResolution;
    Set<URI> conformsTo;

    // Enhanced properties for DCAT2
    URI mediaType;       // e.g. application/json
    URI format;          // e.g. JSON
    URI packageFormat;   // format of the packaging (zip, tar)
    URI compressFormat;  // compression format if compressed
    
    // Additional properties
    String checksum;
    String checksumAlgorithm;
    Long fileSize; // in bytes
    String documentation;
    String language;
    
    /**
     * Parent dataset that this distribution belongs to.
     * This reverse relationship isn't directly modeled in DCAT2 but is useful in Java.
     */
    Dataset dataset;
}
