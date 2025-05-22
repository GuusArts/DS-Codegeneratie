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

/**
 * Represents a DCAT2 Distribution.
 * 
 * In DCAT2, a Distribution is a specific representation of a dataset. A dataset might be available in multiple
 * serializations that may differ in various ways, including natural language, media-type or format,
 * schematic organization, temporal and spatial resolution, level of detail or profiles.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Distribution">DCAT2 Distribution</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Distribution extends Resource {

    /**
     * A URL that is a direct link to a downloadable file in a given format.
     */
    private URI downloadURL;

    /**
     * A URL of a resource that gives access to a distribution of the dataset.
     */
    private URI accessURL;

    /**
     * The media type of the distribution as defined by IANA.
     */
    private String mediaType;

    /**
     * The file format of the distribution.
     */
    private String format;

    /**
     * The size of a distribution in bytes.
     */
    private Long byteSize;

    /**
     * The compression format of the distribution in which the data is contained in a compressed form.
     */
    private String compressFormat;

    /**
     * The package format of the distribution in which one or more data files are grouped together.
     */
    private String packageFormat;

    /**
     * Information about rights held in and over the distribution.
     */
    private URI license;

    /**
     * The date when the distribution was issued.
     */
    private ZonedDateTime issued;

    /**
     * The date when the distribution was last modified.
     */
    private ZonedDateTime modified;
}
