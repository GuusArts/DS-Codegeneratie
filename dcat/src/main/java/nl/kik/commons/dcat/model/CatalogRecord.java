package nl.kik.commons.dcat.model;

import java.net.URI;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * Represents a DCAT2 CatalogRecord.
 * 
 * In DCAT2, a CatalogRecord is a record in a data catalog, describing a single dataset or data service.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog_Record">DCAT2 CatalogRecord</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CatalogRecord extends RDFObject {
    
    /**
     * A free-text account of the record.
     */
    private String description;
    
    /**
     * A name given to the record.
     */
    private String title;
    
    /**
     * The resource (dataset or data service) described by this record.
     */
    private Resource resource;
    
    /**
     * The date when the record was issued.
     */
    private ZonedDateTime issued;
    
    /**
     * The date when the record was last modified.
     */
    private ZonedDateTime modified;
    
    /**
     * The primary language of the record.
     */
    private URI language;
    
    /**
     * The status of the record in the context of the catalog.
     */
    private URI status;
    
    /**
     * The date of the most recent change in the status of the record.
     */
    private ZonedDateTime statusModified;
    
    /**
     * The entity primarily responsible for maintaining the record.
     */
    private URI publisher;
    
    /**
     * The license under which the record is published.
     */
    private URI license;
}