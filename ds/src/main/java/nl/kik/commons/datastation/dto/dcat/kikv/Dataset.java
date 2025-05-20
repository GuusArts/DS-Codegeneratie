package nl.kik.commons.datastation.dto.dcat.kikv;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.util.Date;

// ... existing code ...

public class Dataset {
    // Basic properties based on DCAT2
    private String id;
    private String title;
    private String description;
    private List<Distribution> distributions;
    private String publisher; // Represents dcat:publisher
    private Date issued; // Represents dct:issued
    private Date modified; // Represents dct:modified
    private URI license; // Represents dct:license
    private List<String> keyword; // Represents dcat:keyword
    private List<URI> theme; // Represents dcat:theme
    private String contactPoint; // Represents dcat:contactPoint (can be VCard or Org)
    private Double spatialResolutionInMeters; // Represents dcat:spatialResolutionInMeters
    // Add more properties as needed, e.g., publisher, distributions list

    public Dataset() {
        this.distributions = new ArrayList<>();
        this.keyword = new ArrayList<>();
        this.theme = new ArrayList<>();
    }

    public Dataset(String id, String title, String description, List<Distribution> distributions, String publisher, Date issued, Date modified, URI license, List<String> keyword, List<URI> theme, String contactPoint, Double spatialResolutionInMeters) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.distributions = distributions != null ? distributions : new ArrayList<>();
        this.publisher = publisher;
        this.issued = issued;
        this.modified = modified;
        this.license = license;
        this.keyword = keyword != null ? keyword : new ArrayList<>();
        this.theme = theme != null ? theme : new ArrayList<>();
        this.contactPoint = contactPoint;
        this.spatialResolutionInMeters = spatialResolutionInMeters;
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

    public List<Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<Distribution> distributions) {
        this.distributions = distributions;
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

    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {
        this.keyword = keyword;
    }

    public List<URI> getTheme() {
        return theme;
    }

    public void setTheme(List<URI> theme) {
        this.theme = theme;
    }

    public String getContactPoint() {
        return contactPoint;
    }

    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    public Double getSpatialResolutionInMeters() {
        return spatialResolutionInMeters;
    }

    public void setSpatialResolutionInMeters(Double spatialResolutionInMeters) {
        this.spatialResolutionInMeters = spatialResolutionInMeters;
    }

    // Add getters and setters for other properties later
} 