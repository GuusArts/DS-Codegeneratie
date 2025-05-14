package nl.kik.commons.datastation.dto.dcat2;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.dcat.DataService;

/**
 * Represents a data service in DCAT2.
 * Extends the existing DataService class to add DCAT2-specific properties.
 * 
 * A data service is a collection of operations that provides access to one or more datasets or data processing functions.
 */
@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class DataService2 extends DataService {

    /**
     * The license document for the service.
     */
    private URI license;

    /**
     * The access rights for the service.
     */
    private URI accessRights;

    /**
     * Temporal properties of the service.
     */
    private ResourceTimeEvolution timeEvolution;
}
