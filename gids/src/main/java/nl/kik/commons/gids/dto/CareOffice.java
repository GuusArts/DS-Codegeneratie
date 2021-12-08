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
public class CareOffice extends GidsObject implements HasName, Projectable<Source, CareOffice>, Comparable<CareOffice> {
	private GidsAttribute<String> code;
	private GidsAttribute<Region> region;
	private GidsAttribute<Concessionaire> concessionaire;
	private GidsAttribute<String> name;

	@Override
	public int compareTo(final CareOffice o) {
		if (o == null) {
			return 1;
		}
		int result = compare(code, o.code);
		if (result != 0) {
			return result;
		}
		result = compare(region, o.region);
		if (result != 0) {
			return result;
		}
		result = compare(concessionaire, o.concessionaire);
		if (result != 0) {
			return result;
		}
		result = compare(name, o.name);
		return result;
	}

	public CareOffice orNull() {
		if (getId() == null && code == null && region == null && concessionaire == null && name == null) {
			return null;
		}
		return this;
	}

	@Override
	public CareOffice project(final Source key, final LocalDate date) {
		return CareOffice.builder() //
				.id(getId()) //
				.code(code == null ? null : code.project(key, date)) //
				.region(region == null ? null : region.project(key, date)) //
				.concessionaire(concessionaire == null ? null : concessionaire.project(key, date)) //
				.name(name == null ? null : name.project(key, date)) //
				.build().orNull();
	}

}
