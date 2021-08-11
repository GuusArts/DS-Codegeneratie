package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CatalogRecord extends DCATObject {
	String title;
	String description;
	ZonedDateTime issued;
	ZonedDateTime modified;
//	foaf:Resource primaryTopic;
	URI conformsTo; // Standard
}
