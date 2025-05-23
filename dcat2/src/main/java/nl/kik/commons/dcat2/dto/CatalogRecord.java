package nl.kik.commons.dcat2.dto;

import java.net.URI;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A DCAT CatalogRecord.
 * 
 * A catalog record is a metadata record describing a specific dataset or data service inclusion in the catalog.
 * 
 * As per DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog_Record
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CatalogRecord extends DCATObject {
    
    /**
     * The title of the catalog record.
     */
    private String title;
    
    /**
     * A description of the catalog record.
     */
    private String description;
    
    /**
     * Date of formal issuance of the catalog record.
     */
    private LocalDateTime issued;
    
    /**
     * Most recent date on which the catalog record was modified.
     */
    private LocalDateTime modified;
    
    /**
     * The dataset or data service described by this record.
     */
    private URI primaryTopic;
    
    /**
     * The status of the catalog record, e.g., completed, deprecated, withdrawn, etc.
     */
    private URI conformsTo;
    
    /**
     * The status of the catalog record, e.g., completed, deprecated, withdrawn, etc.
     */
    private String recordStatus;
    
    /**
     * Link to the source metadata.
     */
    private URI sourceMetadata;
}
