package nl.kik.commons.datastation.dto.dcat2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.dcat.DCATObject;

/**
 * Base class for all DCAT2 objects.
 * Extends the existing DCATObject to maintain compatibility.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DCAT2Object extends DCATObject {
}
