package nl.kik.commons.datastation.dto.dcat.kikv;

import java.net.URI;

// ... existing code ...

public class Distribution {
    // Basic properties based on DCAT2
    private String id;
    private String title;
    private String description;
    private String format; // Represents dct:format
    private String mediaType; // Represents dcat:mediaType
    private URI downloadURL; // Represents dcat:downloadURL
    private Long byteSize; // Represents dcat:byteSize
    private String checksum; // Represents dcat:checksum (can be a Checksum object in a more detailed model)
    private URI accessURL; // Represents dcat:accessURL

    public Distribution() {
    }

    public Distribution(String id, String title, String description, String format, String mediaType, URI downloadURL, Long byteSize, String checksum, URI accessURL) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.format = format;
        this.mediaType = mediaType;
        this.downloadURL = downloadURL;
        this.byteSize = byteSize;
        this.checksum = checksum;
        this.accessURL = accessURL;
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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public URI getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(URI downloadURL) {
        this.downloadURL = downloadURL;
    }

    public Long getByteSize() {
        return byteSize;
    }

    public void setByteSize(Long byteSize) {
        this.byteSize = byteSize;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public URI getAccessURL() {
        return accessURL;
    }

    public void setAccessURL(URI accessURL) {
        this.accessURL = accessURL;
    }

    // Add getters and setters for other properties later
} 