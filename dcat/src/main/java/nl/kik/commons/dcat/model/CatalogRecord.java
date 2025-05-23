package nl.kik.commons.dcat.model;

import java.net.URI;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * A DCAT CatalogRecord is a record in a catalog, describing the registration of a single dataset or data service.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-dcat-2/#Class:Catalog_Record">DCAT2 CatalogRecord</a>
 */
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CatalogRecord extends RDFObject {

    /**
     * The date of formal issuance (e.g., publication) of the catalog record.
     */
    private ZonedDateTime issued;

    /**
     * The most recent date on which the catalog record was modified.
     */
    @NotNull
    private ZonedDateTime modified;

    /**
     * The dataset or data service that is described by this catalog record.
     */
    @NotNull
    private URI primaryTopic;

    /**
     * The status of the dataset or data service in the context of this catalog record.
     */
    private URI conformsTo;

    /**
     * The description of the dataset or data service in the context of this catalog record.
     */
    private String description;

    /**
     * The title of the dataset or data service in the context of this catalog record.
     */
    private String title;

    /**
     * Links the catalog record to the resource (dataset or data service) described in the record.
     * 
     * @param resource The resource (dataset or data service) to link
     */
    public void setPrimaryTopic(RDFObject resource) {
        if (resource != null && resource.getId() != null) {
            this.primaryTopic = URI.create(resource.getId());
        }
    }
}