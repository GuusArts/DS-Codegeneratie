package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A record in a data catalog, describing a single dataset or data service.
 * Based on DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog_Record
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CatalogRecord extends DCATObject {
    // From existing implementation
    String title;
    String description;
    ZonedDateTime issued;
    ZonedDateTime modified;
    Resource primaryTopic;
    Set<URI> conformsTo;
    
    // New DCAT2 properties
    ZonedDateTime listingDate;    // The date this record was added to the catalog
    String language;              // The language of this record
    URI sourceMetadata;           // Link to the source metadata
    ZonedDateTime validUntil;     // Date until which the record is valid
    
    /**
     * The parent catalog that contains this record.
     * This reverse relationship isn't directly modeled in DCAT2 but is useful in Java.
     */
    Catalog catalog;
}
