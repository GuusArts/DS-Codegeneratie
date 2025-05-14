package nl.kik.commons.datastation.dto.dcat2;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents the temporal evolution of a resource.
 * This is a new class introduced in DCAT2 to track how resources change over time.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class ResourceTimeEvolution extends DCAT2Object {
    
    /**
     * The date when the resource was first created or issued.
     */
    private ZonedDateTime created;
    
    /**
     * The date when the resource was last modified.
     */
    private ZonedDateTime modified;
    
    /**
     * The date when the resource was made available.
     */
    private ZonedDateTime issued;
    
    /**
     * The date when the resource was or will be retired or superseded.
     */
    private ZonedDateTime deprecated;
    
    /**
     * The date when the resource was or will be destroyed.
     */
    private ZonedDateTime withdrawn;
}
