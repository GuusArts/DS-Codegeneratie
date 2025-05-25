package nl.kik.commons.dcat2.dto;

import java.util.List;

public class Catalog {
    private String id;
    private String title;
    private String description;
    private List<Dataset> datasets;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Dataset> getDatasets() { return datasets; }
    public void setDatasets(List<Dataset> datasets) { this.datasets = datasets; }
} 