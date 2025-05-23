package nl.kik.commons.dcat2.dto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * A DCAT Catalog.
 * 
 * A catalog represents a collection of datasets or data services.
 * As per DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Catalog extends DCATObject {
    
    /**
     * The title of the catalog.
     */
    private String title;
    
    /**
     * A description of the catalog.
     */
    private String description;
    
    /**
     * The license under which this catalog can be used or reused.
     */
    private URI license;
    
    /**
     * The entity primarily responsible for making the catalog.
     */
    private Agent publisher;
    
    /**
     * The entity responsible for making the catalog online.
     */
    private Agent creator;
    
    /**
     * The ISO 639-1 language code for the catalog.
     */
    private List<String> language;
    
    /**
     * Datasets that are part of this catalog.
     */
    private List<Dataset> datasets;
    
    /**
     * Data services that are part of this catalog.
     */
    private List<DataService> services;
    
    /**
     * Date of formal issuance of the catalog.
     */
    private LocalDateTime issued;
    
    /**
     * Most recent date on which the catalog was modified.
     */
    private LocalDateTime modified;
    
    /**
     * An established taxonomy of themes or categories used to classify the datasets.
     */
    private List<URI> themeTaxonomy;
    
    /**
     * A landing page, feed, or other type of resource that gives access to the catalog.
     */
    private List<URI> homepage;
}
