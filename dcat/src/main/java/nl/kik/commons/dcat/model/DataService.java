package nl.kik.commons.dcat.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * A DCAT DataService is a collection of operations that provides access to one or more datasets or data processing functions.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Data_Service">DCAT2 DataService</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DataService extends RDFObject {

    /**
     * A name given to the data service.
     */
    @NotBlank
    private String title;

    /**
     * A free-text account of the data service.
     */
    private String description;

    /**
     * The date of formal issuance (e.g., publication) of the data service.
     */
    private ZonedDateTime issued;

    /**
     * The most recent date on which the data service was modified.
     */
    private ZonedDateTime modified;

    /**
     * The URL of the service endpoint.
     */
    @NotNull
    private URI endpointURL;

    /**
     * A description of the service endpoint, including its operations, parameters, etc.
     */
    private URI endpointDescription;

    /**
     * The license under which the data service can be used.
     */
    private URI license;

    /**
     * The entity responsible for making the data service available.
     */
    private URI publisher;

    /**
     * A dataset that can be distributed by this data service.
     */
    private List<Dataset> servesDataset;

    /**
     * Adds a dataset that can be distributed by this data service.
     * 
     * @param dataset The dataset to add
     */
    public void addServesDataset(Dataset dataset) {
        if (this.servesDataset == null) {
            this.servesDataset = new ArrayList<>();
        }
        this.servesDataset.add(dataset);
    }
}