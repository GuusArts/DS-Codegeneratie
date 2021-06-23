package nl.kik.commons.gids.dto;

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
public class Organisation extends GidsObject
		implements HasName, HasAgb, HasAddress, Changeable, Projectable<Source, Organisation> {
	private GidsAttribute<Address> address;
	private GidsAttribute<CareOffice> office;
	private GidsAttribute<String> name;
	private GidsAttribute<String> tradeName;
	private GidsAttribute<String> careProviderName;
	private GidsAttribute<ZonedDateTime> lastModified;
	private GidsAttribute<String> agb;
	private List<GidsAttribute<Location>> location;
	private GidsAttribute<DeliveryMethod> deliveryMethod;

	@Override
	public Organisation project(Source key) {
		return Organisation.builder() //
				.address(address == null ? null : address.project(key)) //
				.office(office == null ? null : office.project(key)) //
				.name(name == null ? null : name.project(key)) //
				.tradeName(tradeName == null ? null : tradeName.project(key)) //
				.careProviderName(careProviderName == null ? null : careProviderName.project(key)) //
				.lastModified(lastModified == null ? null : lastModified.project(key)) //
				.agb(agb == null ? null : agb.project(key)) //
				.location(location == null ? null
						: location.stream().map(l -> l.project(key)).filter(Objects::nonNull)
								.collect(Collectors.toList())) //
				.deliveryMethod(deliveryMethod == null ? null : deliveryMethod.project(key)) //
				.build().orNull();
	}

	public Organisation orNull() {
		if (address == null && office == null && name == null && tradeName == null && careProviderName == null
				&& lastModified == null && agb == null && (location == null || location.isEmpty())
				&& deliveryMethod == null) {
			return null;
		} else {
			return this;
		}
	}

}
