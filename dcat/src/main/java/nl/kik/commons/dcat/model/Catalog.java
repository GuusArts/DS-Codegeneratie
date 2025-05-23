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
 * A DCAT Catalog is a curated collection of metadata about resources (e.g., datasets and data services).
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog">DCAT2 Catalog</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Catalog extends RDFObject {

    /**
     * A name given to the catalog.
     */
    @NotBlank
    private String title;

    /**
     * A free-text account of the catalog.
     */
    private String description;

    /**
     * The date of formal issuance (e.g., publication) of the catalog.
     */
    private ZonedDateTime issued;

    /**
     * The most recent date on which the catalog was modified.
     */
    private ZonedDateTime modified;

    /**
     * The language of the catalog. This refers to the language used in the textual metadata describing titles, 
     * descriptions, etc. of the datasets in the catalog.
     */
    private List<String> language;

    /**
     * The homepage of the catalog.
     */
    private URI homepage;

    /**
     * The license under which the catalog can be used or reused.
     */
    private URI license;

    /**
     * The entity responsible for making the catalog available.
     */
    private URI publisher;

    /**
     * The geographical area covered by the catalog.
     */
    private URI spatial;

    /**
     * The knowledge organization system (KOS) used to classify the catalog's datasets.
     */
    private List<URI> themeTaxonomy;

    /**
     * A dataset that is listed in the catalog.
     */
    private List<Dataset> dataset;

    /**
     * A service that is listed in the catalog.
     */
    private List<DataService> service;

    /**
     * A record describing the registration of a single dataset or data service that is part of the catalog.
     */
    private List<CatalogRecord> record;

    /**
     * Adds a dataset to the catalog.
     * 
     * @param dataset The dataset to add
     */
    public void addDataset(Dataset dataset) {
        if (this.dataset == null) {
            this.dataset = new ArrayList<>();
        }
        this.dataset.add(dataset);
    }

    /**
     * Adds a data service to the catalog.
     * 
     * @param service The data service to add
     */
    public void addService(DataService service) {
        if (this.service == null) {
            this.service = new ArrayList<>();
        }
        this.service.add(service);
    }

    /**
     * Adds a catalog record to the catalog.
     * 
     * @param record The catalog record to add
     */
    public void addRecord(CatalogRecord record) {
        if (this.record == null) {
            this.record = new ArrayList<>();
        }
        this.record.add(record);
    }
}