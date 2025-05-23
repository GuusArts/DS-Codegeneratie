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
 * A DCAT Dataset.
 * 
 * A dataset represents a collection of data, published or curated by a single agent.
 * As per DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Dataset
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Dataset extends DCATObject {
    
    /**
     * The title of the dataset.
     */
    private String title;
    
    /**
     * A description of the dataset.
     */
    private String description;
    
    /**
     * The entity primarily responsible for making the dataset.
     */
    private Agent publisher;
    
    /**
     * The entity responsible for creating the dataset.
     */
    private Agent creator;
    
    /**
     * Distributions of the dataset.
     */
    private List<Distribution> distributions;
    
    /**
     * Keywords or tags describing the dataset.
     */
    private List<String> keywords;
    
    /**
     * A Web page that provides access to the dataset, its distributions and/or additional information.
     */
    private List<URI> landingPage;
    
    /**
     * Main themes or categories of the dataset.
     */
    private List<URI> theme;
    
    /**
     * The license under which the dataset can be used.
     */
    private URI license;
    
    /**
     * The frequency at which the dataset is published.
     */
    private URI accrualPeriodicity;
    
    /**
     * The ISO 639-1 language code of the dataset.
     */
    private List<String> language;
    
    /**
     * Date of formal issuance of the dataset.
     */
    private LocalDateTime issued;
    
    /**
     * Most recent date on which the dataset was modified.
     */
    private LocalDateTime modified;
    
    /**
     * The temporal period that the dataset covers.
     */
    private PeriodOfTime temporal;
    
    /**
     * The geographical area covered by the dataset.
     */
    private Location spatial;
    
    /**
     * Version number of the dataset.
     */
    private String version;
    
    /**
     * Catalog which contains this dataset.
     */
    private URI inCatalog;
    
    /**
     * Link to the data services serving this dataset.
     */
    private List<DataService> servingDataServices;
}
