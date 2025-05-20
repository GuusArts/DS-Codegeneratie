package nl.kik.commons.datastation.dto.dcat.kikv;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.util.Date;

// ... existing code ...

public class Catalog {
    // Basic properties based on DCAT2
    private String id;
    private String title;
    private String description;
    private List<Dataset> datasets;
    private String publisher; // Represents dcat:publisher (can be a VCard, Org, or Agent)
    private Date issued; // Represents dct:issued
    private Date modified; // Represents dct:modified
    private URI license; // Represents dct:license
    private List<URI> themeTaxonomy; // Represents dcat:themeTaxonomy
    // Add more properties as needed, e.g., publisher, datasets list

    public Catalog() {
        this.datasets = new ArrayList<>();
        this.themeTaxonomy = new ArrayList<>();
    }

    public Catalog(String id, String title, String description, List<Dataset> datasets, String publisher, Date issued, Date modified, URI license, List<URI> themeTaxonomy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.datasets = datasets != null ? datasets : new ArrayList<>();
        this.publisher = publisher;
        this.issued = issued;
        this.modified = modified;
        this.license = license;
        this.themeTaxonomy = themeTaxonomy != null ? themeTaxonomy : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Date getIssued() {
        return issued;
    }

    public void setIssued(Date issued) {
        this.issued = issued;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public URI getLicense() {
        return license;
    }

    public void setLicense(URI license) {
        this.license = license;
    }

    public List<URI> getThemeTaxonomy() {
        return themeTaxonomy;
    }

    public void setThemeTaxonomy(List<URI> themeTaxonomy) {
        this.themeTaxonomy = themeTaxonomy;
    }

    // Add getters and setters for other properties later
} 