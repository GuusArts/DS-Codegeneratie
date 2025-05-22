package nl.kik.commons.dcat.model;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a DCAT2 Catalog.
 * 
 * In DCAT2, a Catalog is a curated collection of metadata about resources (e.g., datasets and data services).
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog">DCAT2 Catalog</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Catalog extends Resource {
    
    /**
     * The datasets in this catalog.
     */
    private Set<Dataset> datasets = new HashSet<>();
    
    /**
     * The data services in this catalog.
     */
    private Set<DataService> services = new HashSet<>();
    
    /**
     * The catalogs in this catalog.
     */
    private Set<Catalog> catalogs = new HashSet<>();
    
    /**
     * The catalog records in this catalog.
     */
    private Set<CatalogRecord> records = new HashSet<>();
    
    /**
     * The homepage of the catalog.
     */
    private URI homepage;
    
    /**
     * The date when the catalog was issued.
     */
    private ZonedDateTime issued;
    
    /**
     * The date when the catalog was last modified.
     */
    private ZonedDateTime modified;
    
    /**
     * The language of the catalog.
     */
    private URI language;
    
    /**
     * The license of the catalog.
     */
    private URI license;
    
    /**
     * Adds a dataset to the catalog.
     * 
     * @param dataset The dataset to add
     * @return This catalog for method chaining
     */
    public Catalog addDataset(Dataset dataset) {
        if (datasets == null) {
            datasets = new HashSet<>();
        }
        datasets.add(dataset);
        return this;
    }
    
    /**
     * Adds a service to the catalog.
     * 
     * @param service The service to add
     * @return This catalog for method chaining
     */
    public Catalog addService(DataService service) {
        if (services == null) {
            services = new HashSet<>();
        }
        services.add(service);
        return this;
    }
    
    /**
     * Adds a catalog to the catalog.
     * 
     * @param catalog The catalog to add
     * @return This catalog for method chaining
     */
    public Catalog addCatalog(Catalog catalog) {
        if (catalogs == null) {
            catalogs = new HashSet<>();
        }
        catalogs.add(catalog);
        return this;
    }
    
    /**
     * Adds a record to the catalog.
     * 
     * @param record The record to add
     * @return This catalog for method chaining
     */
    public Catalog addRecord(CatalogRecord record) {
        if (records == null) {
            records = new HashSet<>();
        }
        records.add(record);
        return this;
    }
}