package nl.kik.commons.datastation.dto.dcat2;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.dcat.Relationship;
import nl.kik.commons.dto.RDFObject;

/**
 * Represents a relationship between resources in DCAT2.
 * Extends the existing Relationship class to add DCAT2-specific properties.
 * 
 * A relationship describes the relation between resources.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Relationship2 extends Relationship {
    
    /**
     * The citation for this relationship.
     */
    private String citation;
    
    /**
     * The resources that are the object of this relationship.
     */
    private Set<RDFObject> hasTarget;
}
