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
public class Region extends GidsObject implements Projectable<Source, Region> {
	private GidsAttribute<String> code;

	@Override
	public Region project(Source key) {
		return Region.builder() //
				.code(code == null ? null : code.project(key)) //
				.build().orNull();
	}

	public Region orNull() {
		if (code == null) {
			return null;
		} else {
			return this;
		}
	}

}
