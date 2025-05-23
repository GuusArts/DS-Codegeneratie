package nl.kik.commons.dcat.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * A DCAT Dataset is a collection of data, published or curated by a single agent, and available for access or download in one or more representations.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Dataset">DCAT2 Dataset</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Dataset extends RDFObject {

    /**
     * A name given to the dataset.
     */
    @NotBlank
    private String title;

    /**
     * A free-text account of the dataset.
     */
    private String description;

    /**
     * The date of formal issuance (e.g., publication) of the dataset.
     */
    private ZonedDateTime issued;

    /**
     * The most recent date on which the dataset was modified.
     */
    private ZonedDateTime modified;

    /**
     * The frequency at which the dataset is updated.
     */
    private URI accrualPeriodicity;

    /**
     * The language of the dataset.
     */
    private List<String> language;

    /**
     * The license under which the dataset can be used or reused.
     */
    private URI license;

    /**
     * The entity responsible for making the dataset available.
     */
    private URI publisher;

    /**
     * The geographical area covered by the dataset.
     */
    private URI spatial;

    /**
     * The temporal period that the dataset covers.
     */
    private String temporal;

    /**
     * A keyword or tag describing the dataset.
     */
    private List<String> keyword;

    /**
     * A main category of the dataset. A dataset can have multiple themes.
     */
    private List<URI> theme;

    /**
     * A web page that can be navigated to in a web browser to gain access to the dataset, its distributions and/or additional information.
     */
    private URI landingPage;

    /**
     * A contact point for the dataset.
     */
    private URI contactPoint;

    /**
     * An available distribution of the dataset.
     */
    private List<Distribution> distribution;

    /**
     * Adds a distribution to the dataset.
     * 
     * @param distribution The distribution to add
     */
    public void addDistribution(Distribution distribution) {
        if (this.distribution == null) {
            this.distribution = new ArrayList<>();
        }
        this.distribution.add(distribution);
    }
}