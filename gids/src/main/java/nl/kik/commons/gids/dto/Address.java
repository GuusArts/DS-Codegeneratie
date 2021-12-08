package nl.kik.commons.gids.dto;

import java.time.LocalDate;

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
public class Address extends GidsObject implements Projectable<Source, Address>, Comparable<Address> {
	private GidsAttribute<String> houseNumber;
	private GidsAttribute<String> houseLetter;
	private GidsAttribute<String> town;
	private GidsAttribute<String> province;
	private GidsAttribute<String> postalcode;
	private GidsAttribute<String> street;

	@Override
	public int compareTo(final Address o) {
		if (o == null) {
			return 1;
		}
		int result = compare(houseNumber, o.houseNumber);
		if (result != 0) {
			return result;
		}
		result = compare(houseLetter, o.houseLetter);
		if (result != 0) {
			return result;
		}
		result = compare(town, o.town);
		if (result != 0) {
			return result;
		}
		result = compare(province, o.province);
		if (result != 0) {
			return result;
		}
		result = compare(postalcode, o.postalcode);
		if (result != 0) {
			return result;
		}
		result = compare(street, o.street);
		return result;
	}

	public Address orNull() {
		if (getId() == null && houseNumber == null && houseLetter == null && town == null && province == null
				&& postalcode == null && street == null) {
			return null;
		}
		return this;
	}

	@Override
	public Address project(final Source key, final LocalDate date) {
		return Address.builder() //
				.id(getId()) //
				.houseNumber(houseNumber == null ? null : houseNumber.project(key, date)) //
				.houseLetter(houseLetter == null ? null : houseLetter.project(key, date)) //
				.town(town == null ? null : town.project(key, date)) //
				.province(province == null ? null : province.project(key, date)) //
				.postalcode(postalcode == null ? null : postalcode.project(key, date)) //
				.street(street == null ? null : street.project(key, date)) //
				.build().orNull();
	}
}
