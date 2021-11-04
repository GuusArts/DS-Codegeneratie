package nl.kik.commons.gids.dto;

import java.time.LocalDate;
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
public class Location extends GidsObject implements HasNames, HasAgb, HasAddress, Projectable<Source, Location> {
	private GidsAttribute<String> primaryName;
	private List<GidsAttribute<String>> name;
	private GidsAttribute<String> number;
	private List<GidsAttribute<String>> agb;
	private GidsAttribute<Address> address;

	public Location orNull() {
		if (getId() == null && (name == null || name.isEmpty()) && primaryName == null && number == null
				&& (agb == null || agb.isEmpty()) && address == null)
			return null;
		return this;
	}

	@Override
	public Location project(final Source key, final LocalDate date) {
		return Location.builder() //
				.id(getId()) //
				.primaryName(primaryName == null ? null : primaryName.project(key, date)) //
				.name(name == null ? null
						: name.stream().map(l -> l.project(key, date)).filter(Objects::nonNull)
								.collect(Collectors.toList())) //
				.number(number == null ? null : number.project(key, date)) //
				.agb(agb == null ? null
						: agb.stream()//
								.filter(Objects::nonNull)//
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.collect(Collectors.toList())) //
				.address(address == null ? null : address.project(key, date)) //
				.build().orNull();
	}

}
