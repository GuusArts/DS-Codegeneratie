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
public class Organisation extends GidsObject implements HasNames, HasAgb, HasSbi, HasKvk, HasAddress, Changeable,
		Projectable<Source, Organisation>, Comparable<Organisation> {
	private GidsAttribute<Address> address;
	private GidsAttribute<CareOffice> office;
	private GidsAttribute<String> primaryName;
	private List<GidsAttribute<String>> name;
	private GidsAttribute<ZonedDateTime> lastModified;
	private List<GidsAttribute<String>> agb;
	private List<GidsAttribute<String>> sbi;
	private GidsAttribute<String> kvk;
	private List<GidsAttribute<Location>> location;
	private GidsAttribute<DeliveryMethod> deliveryMethod;
	private GidsAttribute<String> endpoint;

	@Override
	public int compareTo(final Organisation o) {
		if (o == null) {
			return 1;
		}
		int result = compare(kvk, o.kvk);
		if (result != 0) {
			return result;
		}
		result = compare(endpoint, o.endpoint);
		if (result != 0) {
			return result;
		}
		result = compare(lastModified, o.lastModified);
		if (result != 0) {
			return result;
		}
		result = compare(deliveryMethod, o.deliveryMethod);
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
		result = compare(sbi, o.sbi);
		if (result != 0) {
			return result;
		}
		result = compare(address, o.address);
		if (result != 0) {
			return result;
		}
		result = compare(office, o.office);
		if (result != 0) {
			return result;
		}
		result = compare(location, o.location);
		return result;
	}

	public Organisation orNull() {
		if (getId() == null && address == null && office == null && (name == null || name.isEmpty())
				&& primaryName == null && lastModified == null && (agb == null || agb.isEmpty())
				&& (sbi == null || sbi.isEmpty()) && kvk == null && endpoint == null
				&& (location == null || location.isEmpty()) && deliveryMethod == null) {
			return null;
		}
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
						: orNull(name.stream()//
								.filter(Objects::nonNull) //
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.sorted() //
								.collect(Collectors.toList()))) //
				.lastModified(lastModified == null ? null : lastModified.project(key, date)) //
				.agb(agb == null ? null
						: orNull(agb.stream() //
								.filter(Objects::nonNull) //
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.sorted() //
								.collect(Collectors.toList()))) //
				.sbi(sbi == null ? null
						: orNull(sbi.stream() //
								.filter(Objects::nonNull) //
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.sorted() //
								.collect(Collectors.toList()))) //
				.kvk(kvk == null ? null : kvk.project(key, date)) //
				.endpoint(endpoint == null ? null : endpoint.project(key, date)) //
				.location(location == null ? null
						: orNull(location.stream() //
								.filter(Objects::nonNull) //
								.map(l -> l.project(key, date)) //
								.filter(Objects::nonNull) //
								.sorted() //
								.collect(Collectors.toList()))) //
				.deliveryMethod(deliveryMethod == null ? null : deliveryMethod.project(key, date)) //
				.build()).orNull();
	}

}
