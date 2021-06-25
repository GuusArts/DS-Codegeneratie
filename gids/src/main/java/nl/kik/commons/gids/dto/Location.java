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
public class Location extends GidsObject implements HasName, HasAgb, HasKvk, Projectable<Source, Location> {
	private GidsAttribute<String> name;
	private GidsAttribute<String> number;
	private GidsAttribute<String> agb;
	private GidsAttribute<String> kvk;

	@Override
	public Location project(Source key) {
		return Location.builder() //
				.id(getId()) //
				.name(name == null ? null : name.project(key)) //
				.number(number == null ? null : number.project(key)) //
				.agb(agb == null ? null : agb.project(key)) //
				.kvk(kvk == null ? null : kvk.project(key)) //
				.build().orNull();
	}

	public Location orNull() {
		if (name == null && number == null && agb == null && kvk == null) {
			return null;
		} else {
			return this;
		}
	}

}
