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
public class CareOffice extends GidsObject implements HasName, Projectable<Source, CareOffice> {
	private GidsAttribute<String> code;
	private GidsAttribute<Region> region;
	private GidsAttribute<Concessionaire> concessionaire;
	private GidsAttribute<String> name;

	@Override
	public CareOffice project(Source key) {
		return CareOffice.builder() //
				.code(code == null ? null : code.project(key)) //
				.region(region == null ? null : region.project(key)) //
				.concessionaire(concessionaire == null ? null : concessionaire.project(key)) //
				.name(name == null ? null : name.project(key)) //
				.build().orNull();
	}

	public CareOffice orNull() {
		if (code == null && region == null && concessionaire == null && name == null) {
			return null;
		} else {
			return this;
		}
	}

}
