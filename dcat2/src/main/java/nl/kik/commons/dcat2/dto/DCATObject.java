package nl.kik.commons.dcat2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

/**
 * Base class for all DCAT2 model objects.
 * Extends RDFObject to inherit the id property and RDF functionality.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public abstract class DCATObject extends RDFObject {
    
}
