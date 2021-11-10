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
public class Location extends GidsObject
		implements HasNames, HasAgb, HasSbi, HasAddress, Projectable<Source, Location>, Comparable<Location> {
	private GidsAttribute<String> primaryName;
	private List<GidsAttribute<String>> name;
	private GidsAttribute<String> number;
	private List<GidsAttribute<String>> agb;
	private List<GidsAttribute<String>> sbi;
	private GidsAttribute<Address> address;

	public Location orNull() {
		if (getId() == null && (name == null || name.isEmpty()) && primaryName == null && number == null
				&& (agb == null || agb.isEmpty()) && (sbi == null || sbi.isEmpty()) && address == null)
			return null;
		return this;
	}

	@Override
	public Location project(final Source key, final LocalDate date) {
		return Location.builder() //
				.id(getId()) //
				.primaryName(primaryName == null ? null : primaryName.project(key, date)) //
				.name(name == null ? null
						: orNull(name.stream() //
								.filter(Objects::nonNull)//
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.sorted() //
								.collect(Collectors.toList()))) //
				.number(number == null ? null : number.project(key, date)) //
				.agb(agb == null ? null
						: orNull(agb.stream()//
								.filter(Objects::nonNull)//
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.sorted() //
								.collect(Collectors.toList()))) //
				.sbi(sbi == null ? null
						: orNull(sbi.stream()//
								.filter(Objects::nonNull)//
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.sorted() //
								.collect(Collectors.toList()))) //
				.address(address == null ? null : address.project(key, date)) //
				.build().orNull();
	}

	@Override
	public int compareTo(Location o) {
		if (o == null) {
			return 1;
		}
		int result = compare(number, o.number);
		if (result != 0) {
			return result;
		}
		result = compare(name, o.name);
		if (result != 0) {
			return result;
		}
		result = compare(agb, o.agb);
		if (result != 0) {
			return result;
		}
		result = compare(sbi, o.agb);
		if (result != 0) {
			return result;
		}
		result = compare(address, o.address);
		if (result != 0) {
			return result;
		}
		return 0;
	}

}
