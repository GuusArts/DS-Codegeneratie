package nl.kik.commons.gids.dto;

import java.time.LocalDate;
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
public class Organisation extends GidsObject
		implements HasNames, HasAgb, HasKvk, HasAddress, Changeable, Projectable<Source, Organisation> {
	private GidsAttribute<Address> address;
	private GidsAttribute<CareOffice> office;
	private GidsAttribute<String> primaryName;
	private List<GidsAttribute<String>> name;
	private GidsAttribute<ZonedDateTime> lastModified;
	private List<GidsAttribute<String>> agb;
	private GidsAttribute<String> kvk;
	private List<GidsAttribute<Location>> location;
	private GidsAttribute<DeliveryMethod> deliveryMethod;

	public Organisation orNull() {
		if (getId() == null && address == null && office == null && (name == null || name.isEmpty())
				&& primaryName == null && lastModified == null && (agb == null || agb.isEmpty()) && kvk == null
				&& (location == null || location.isEmpty()) && deliveryMethod == null)
			return null;
		return this;
	}

	@Override
	public Organisation project(final Source key, final LocalDate date) {
		return ((Organisation) Organisation.builder() //
				.id(getId()) //
				.address(address == null ? null : address.project(key, date)) //
				.office(office == null ? null : office.project(key, date)) //
				.primaryName(primaryName == null ? null : primaryName.project(key, date)) //
				.name(name == null ? null
						: name.stream().map(l -> l.project(key, date)).filter(Objects::nonNull)
								.collect(Collectors.toList())) //
				.lastModified(lastModified == null ? null : lastModified.project(key, date)) //
				.agb(agb == null ? null
						: agb.stream() //
								.filter(Objects::nonNull) //
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.collect(Collectors.toList())) //
				.kvk(kvk == null ? null : kvk.project(key, date)) //
				.location(location == null ? null
						: location.stream() //
								.filter(Objects::nonNull) //
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.collect(Collectors.toList())) //
				.deliveryMethod(deliveryMethod == null ? null : deliveryMethod.project(key, date)) //
				.build()).orNull();
	}

}
