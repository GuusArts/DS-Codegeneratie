package nl.kik.commons.gids.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.Projectable;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class Location extends GidsObject implements HasName, HasAgb, HasAddress, Projectable<Source, Location> {
	private GidsAttribute<String> name;
	private GidsAttribute<String> number;
	private List<GidsAttribute<String>> agb;
	private GidsAttribute<Address> address;

	@Override
	public Location project(Source key, ZonedDateTime date) {
		return Location.builder() //
				.id(getId()) //
				.name(name == null ? null : name.project(key, date)) //
				.number(number == null ? null : number.project(key, date)) //
				.agb(agb == null ? null
						: agb.stream().map(l -> l.project(key, date)).filter(Objects::nonNull)
								.collect(Collectors.toList())) //
				.address(address == null ? null : address.project(key, date)) //
				.build().orNull();
	}

	public Location orNull() {
		if (name == null && number == null && (agb == null || agb.isEmpty()) && address == null) {
			return null;
		} else {
			return this;
		}
	}

}
