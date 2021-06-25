package nl.kik.commons.gids.dto;

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
public class Address extends GidsObject implements Projectable<Source, Address> {
	private GidsAttribute<String> houseNumber;
	private GidsAttribute<String> houseLetter;
	private GidsAttribute<String> town;
	private GidsAttribute<String> province;
	private GidsAttribute<String> postalcode;
	private GidsAttribute<String> street;

	public Address project(Source key) {
		return Address.builder() //
				.id(getId()) //
				.houseNumber(houseNumber == null ? null : houseNumber.project(key)) //
				.houseLetter(houseLetter == null ? null : houseLetter.project(key)) //
				.town(town == null ? null : town.project(key)) //
				.province(province == null ? null : province.project(key)) //
				.postalcode(postalcode == null ? null : postalcode.project(key)) //
				.street(street == null ? null : street.project(key)) //
				.build().orNull();
	}

	public Address orNull() {
		if (houseNumber == null && houseLetter == null && town == null && province == null && postalcode == null
				&& street == null) {
			return null;
		} else {
			return this;
		}
	}
}
