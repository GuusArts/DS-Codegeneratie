package nl.kik.commons.gids.dto;

import java.time.ZonedDateTime;
import java.util.List;

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
public class Organisation extends GidsObject implements HasName, HasAgb {
	private Address address;
	private Zorgkantoor zorgkantoor;
	private String name;
	private String handelsnaam;
	private String zorgverlenernaam;
	private ZonedDateTime lastModified;
	private String agb;
	private List<Location> location;
	private DeliveryMethod deliveryMethod;
}
