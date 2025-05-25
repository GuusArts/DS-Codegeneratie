package nl.kik.commons.dcat2.dto;

import java.util.List;

public class Dataset {
    private String id;
    private String title;
    private String description;
    private List<Distribution> distributions;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Distribution> getDistributions() { return distributions; }
    public void setDistributions(List<Distribution> distributions) { this.distributions = distributions; }
} 