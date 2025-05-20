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
 * A DCAT Distribution.
 * 
 * A Distribution represents a specific available form of a dataset.
 * Each dataset might be available in different forms, e.g. different formats
 * or different endpoints. Examples of distributions include a downloadable CSV file, an API, or an RSS feed.
 * 
 * As per DCAT2 specification: https://www.w3.org/TR/vocab-dcat-2/#Class:Distribution
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Distribution extends DCATObject {
    
    /**
     * The title of the distribution.
     */
    private String title;
    
    /**
     * A description of the distribution.
     */
    private String description;
    
    /**
     * A URL that gives access to a distribution of the dataset.
     * This can be a direct download link, a link to an HTML page containing a link to the actual data, or an API URL.
     */
    private URI accessURL;
    
    /**
     * A direct link to a downloadable file in a given format.
     */
    private URI downloadURL;
    
    /**
     * The size of the distribution in bytes.
     */
    private Long byteSize;
    
    /**
     * The IANA media type of the distribution.
     */
    private String mediaType;
    
    /**
     * The format of the distribution, e.g., CSV, XML, JSON, etc.
     */
    private String format;
    
    /**
     * Information about the compression format of the distribution.
     */
    private String compressionFormat;
    
    /**
     * Information about the packaging format of the distribution.
     */
    private String packageFormat;
    
    /**
     * The license under which the distribution is made available.
     */
    private URI license;
    
    /**
     * Date of formal issuance of the distribution.
     */
    private LocalDateTime issued;
    
    /**
     * Most recent date on which the distribution was modified.
     */
    private LocalDateTime modified;
    
    /**
     * Rights statement about the distribution.
     */
    private URI rights;
    
    /**
     * The data service that provides access to this distribution.
     */
    private DataService accessService;
}
