package nl.kik.commons.dcat.model;

import java.net.URI;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * A DCAT Distribution represents a specific representation of a dataset. A dataset might be available in multiple serializations that may differ in various ways, including natural language, media-type or format, schematic organization, temporal and spatial resolution, level of detail or profiles (which might specify any or all of the above).
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Distribution">DCAT2 Distribution</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Distribution extends RDFObject {

    /**
     * A name given to the distribution.
     */
    private String title;

    /**
     * A free-text account of the distribution.
     */
    private String description;

    /**
     * The date of formal issuance (e.g., publication) of the distribution.
     */
    private ZonedDateTime issued;

    /**
     * The most recent date on which the distribution was modified.
     */
    private ZonedDateTime modified;

    /**
     * The license under which the distribution is made available.
     */
    private URI license;

    /**
     * A URL that gives access to a distribution of the dataset. The resource at the access URL may contain information about how to get the dataset.
     */
    @NotNull
    private URI accessURL;

    /**
     * A URL that is a direct link to a downloadable file in a given format.
     */
    private URI downloadURL;

    /**
     * The size of a distribution in bytes.
     */
    private Long byteSize;

    /**
     * The media type of the distribution as defined by IANA.
     */
    private String mediaType;

    /**
     * The file format of the distribution.
     */
    private String format;

    /**
     * Information about rights held in and over the distribution.
     */
    private URI rights;

    /**
     * A compression format used for the distribution.
     */
    private URI compressFormat;

    /**
     * A package format used for the distribution.
     */
    private URI packageFormat;
}