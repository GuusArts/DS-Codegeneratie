package nl.kik.datastation.dto.dcat;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class CatalogRecord {
	String title;
	String description;
	ZonedDateTime issued;
	ZonedDateTime modified;
//	foaf:Resource primaryTopic;
	Standard conformsTo;
}
