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
public class Concessionaire extends GidsObject implements HasName, Projectable<Source, Concessionaire> {
	private GidsAttribute<String> name;

	@Override
	public Concessionaire project(Source key) {
		return Concessionaire.builder() //
				.name(name == null ? null : name.project(key)) //
				.build().orNull();
	}

	public Concessionaire orNull() {
		if (name == null) {
			return null;
		} else {
			return this;
		}
	}

}
