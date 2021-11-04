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
public class Region extends GidsObject implements Projectable<Source, Region>, Comparable<Region> {
	private GidsAttribute<String> code;

	public Region orNull() {
		if (getId() == null && code == null)
			return null;
		return this;
	}

	@Override
	public Region project(final Source key, final LocalDate date) {
		return Region.builder() //
				.id(getId()) //
				.code(code == null ? null : code.project(key, date)) //
				.build().orNull();
	}

	@Override
	public int compareTo(Region o) {
		if (o == null) {
			return 1;
		}
		int result = compare(code, o.code);
		if (result != 0) {
			return result;
		}
		return 0;
	}

}
