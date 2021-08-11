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
public class Concessionaire extends GidsObject implements HasName, Projectable<Source, Concessionaire> {
	private GidsAttribute<String> name;

	public Concessionaire orNull() {
		if (name == null)
			return null;
		return this;
	}

	@Override
	public Concessionaire project(final Source key, final LocalDate date) {
		return Concessionaire.builder() //
				.id(getId()) //
				.name(name == null ? null : name.project(key, date)) //
				.build().orNull();
	}

}
