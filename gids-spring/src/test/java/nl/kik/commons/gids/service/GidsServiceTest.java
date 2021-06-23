package nl.kik.commons.gids.service;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.kik.commons.dto.Graph;
import nl.kik.commons.gids.dto.Address;
import nl.kik.commons.gids.dto.CareOffice;
import nl.kik.commons.gids.dto.Concessionaire;
import nl.kik.commons.gids.dto.DeliveryMethod;
import nl.kik.commons.gids.dto.GidsAttribute;
import nl.kik.commons.gids.dto.Location;
import nl.kik.commons.gids.dto.Organisation;
import nl.kik.commons.gids.dto.Region;
import nl.kik.commons.gids.dto.Source;
import nl.kik.commons.service.RDFService;

class GidsServiceTest {

	private GidsService service;
	private Address address;
	private CareOffice careOffice;
	private Region region;
	private Concessionaire concessionaire;
	private Location location;
	private Organisation organisation;

	@BeforeEach
	void setUp() throws Exception {
		service = new GidsService();

		region = Region.builder() //
				.code(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "772").build()) //
				.build();

		concessionaire = Concessionaire.builder() //
				.name(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "CZ").build()) //
				.build();

		careOffice = CareOffice.builder() //
				.code(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "5529").build()) //
				.name(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "ZUIDOOST-BRABANT").build()) //
				.concessionaire(
						GidsAttribute.<Concessionaire>builder().alternative(Source.TABELBEHEER, concessionaire).build()) //
				.region(GidsAttribute.<Region>builder().alternative(Source.TABELBEHEER, region).build()) //
				.build();

		location = Location.builder() //
				.name(GidsAttribute.<String>builder().alternative(Source.LRZA, "Britney Health, bv").build()) //
				.number(GidsAttribute.<String>builder().alternative(Source.LRZA, "1").build()) //
				.agb(GidsAttribute.<String>builder().alternative(Source.LRZA, "12345678").build()) //
				.build();

		address = Address.builder() //
				.houseNumber(GidsAttribute.<String>builder().alternative(Source.LRZA, "2").build()) //
				.houseLetter(GidsAttribute.<String>builder().alternative(Source.LRZA, "c").build()) //
				.town(GidsAttribute.<String>builder().alternative(Source.LRZA, "Eindhoven").build()) //
				.province(GidsAttribute.<String>builder().alternative(Source.LRZA, "Noord-Brabant").build()) //
				.postalcode(GidsAttribute.<String>builder().alternative(Source.LRZA, "5621KA").build()) //
				.street(GidsAttribute.<String>builder().alternative(Source.LRZA, "Suikerpeerstraat").build()) //
				.build();

		organisation = Organisation.builder() //
				.address(GidsAttribute.<Address>builder().alternative(Source.LRZA, address).build()) //
				.office(GidsAttribute.<CareOffice>builder().alternative(Source.LRZA, careOffice).build()) //
				.name(GidsAttribute.<String>builder().alternative(Source.LRZA, "Britney Health").build()) //
				.tradeName(GidsAttribute.<String>builder().alternative(Source.LRZA, "Britney Health Intergalactic")
						.build()) //
				.careProviderName(
						GidsAttribute.<String>builder().alternative(Source.LRZA, "Britney Health Loves You").build()) //
				.lastModified(
						GidsAttribute.<ZonedDateTime>builder().alternative(Source.LRZA, ZonedDateTime.now()).build()) //
				.agb(GidsAttribute.<String>builder().alternative(Source.LRZA, "23456789").build()) //
				.location(List.of(GidsAttribute.<Location>builder().alternative(Source.LRZA, location).build())) //
				.deliveryMethod(GidsAttribute.<DeliveryMethod>builder()
						.alternative(Source.KIK_STARTER, DeliveryMethod.KIKStarter).build()) //
				.build();
	}

	@Test
	void testSave() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		service.save(g, region);
		service.save(g, concessionaire);
		service.save(g, careOffice);
		service.save(g, organisation);
		RDFService.snapshot(g, true, null);
	}

}
