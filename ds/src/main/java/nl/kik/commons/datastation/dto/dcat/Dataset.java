package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A collection of data, published or curated by a single agent, and available for access or download in one or more representations.
 * Based on DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Dataset
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Dataset extends Resource {
    // Required properties from existing implementation
    Set<Distribution> distribution;
    URI accrualPeriodicity;
    Float spatialResolutionInMeters;
    PeriodOfTime temporal;
    Duration temporalResolution;
    
    // New DCAT2 properties
    Set<Dataset> isVersionOf;
    Set<Dataset> hasVersion;
    Set<Dataset> previousVersion;
    String version;
    String versionNotes;
    Location spatial;
    
    /**
     * A resource describing a catalog record that contains this dataset.
     * This reverse relationship isn't directly modeled in DCAT2 but is useful in Java.
     */
    Set<CatalogRecord> catalogRecord;
}
