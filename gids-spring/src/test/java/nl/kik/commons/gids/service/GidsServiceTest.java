package nl.kik.commons.gids.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.dto.Graph;
import nl.kik.commons.dto.RDFObject;
import nl.kik.commons.gids.dto.Address;
import nl.kik.commons.gids.dto.CareOffice;
import nl.kik.commons.gids.dto.Concessionaire;
import nl.kik.commons.gids.dto.DeliveryMethod;
import nl.kik.commons.gids.dto.GidsAttribute;
import nl.kik.commons.gids.dto.GidsObject;
import nl.kik.commons.gids.dto.Location;
import nl.kik.commons.gids.dto.Organisation;
import nl.kik.commons.gids.dto.Region;
import nl.kik.commons.gids.dto.Source;
import nl.kik.commons.service.RDFService;

@Slf4j
class GidsServiceTest {
	private GidsService service;
	private Address address;
	private CareOffice careOffice;
	private Region region;
	private Concessionaire concessionaire;
	private Location location;
	private Organisation organisation;
	private List<GidsObject> model;

	@BeforeEach
	void setUp() throws Exception {
		service = new GidsService();

		region = Region.builder() //
				.code(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "772")
						.alternative(Source.LRZA, "772").build()) //
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
				.number(GidsAttribute.<String>builder().alternative(Source.LRZA, "1")
						.alternative(Source.TABELBEHEER, "2").build()) //
				.agb(GidsAttribute.<String>builder().alternative(Source.LRZA, "12345678").build()) //
				.build();

		address = Address.builder() //
				.houseNumber(GidsAttribute.<String>builder().alternative(Source.LRZA, "2")
						.alternative(Source.TABELBEHEER, "2").build()) //
				.houseLetter(GidsAttribute.<String>builder().alternative(Source.LRZA, "c")
						.alternative(Source.TABELBEHEER, "C").build()) //
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
				.lastModified(GidsAttribute.<ZonedDateTime>builder()
						.alternative(Source.LRZA, ZonedDateTime.now().toOffsetDateTime().toZonedDateTime()).build()) //
				.agb(GidsAttribute.<String>builder().alternative(Source.LRZA, "23456789").build()) //
				.location(List.of(GidsAttribute.<Location>builder().alternative(Source.LRZA, location).build())) //
				.deliveryMethod(GidsAttribute.<DeliveryMethod>builder()
						.alternative(Source.KIK_STARTER, DeliveryMethod.KIKStarter).build()) //
				.build();

		model = List.of(region, concessionaire, careOffice, organisation);
	}

	@Test
	void testSave() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		model.forEach(o -> service.save(g, o));
		RDFService.snapshot(g, true, null);
	}

	@Test
	void testProject() {
		Address lrza = address.project(Source.LRZA);
		Address tabelbeheer = address.project(Source.TABELBEHEER);

		log.trace("Projecting");
		log.trace("{}", address);
		log.trace("{}", lrza);
		log.trace("{}", tabelbeheer);

		assertNotEquals(address, lrza);
		assertNotEquals(address, tabelbeheer);
		assertNotEquals(tabelbeheer, lrza);

		assertEquals(address.getHouseNumber().get(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				lrza.getHouseNumber().get());
		assertEquals(address.getHouseLetter().get(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				lrza.getHouseLetter().get());
		assertNotEquals(address.getHouseLetter().get(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				tabelbeheer.getHouseLetter().get());

		assertNotNull(address.getTown());
		assertNotNull(address.getTown().get());
		assertNotNull(lrza.getTown());
		assertNotNull(lrza.getTown().get());
		assertNull(tabelbeheer.getTown());
	}

	@Test
	void testLoadLocal() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		model.forEach(o -> service.save(g, o));
		for (final RDFObject m : model) {
			final Optional<RDFObject> o = service.lookupById(g, m.getId());
			if (o.isEmpty()) {
				Assertions.fail("Not found " + m);
			} else {
				log.trace("Comparing");
				log.trace("{}", m);
				log.trace("{}", o.get());
				Assertions.assertEquals(m, o.get());
				Assertions.assertNotSame(m, o.get());
			}
		}
	}

	@Test
	void testLoadRemote() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		model.forEach(o -> service.save(g, o));

		final DataService metadataService = DataService //
				.newBuilder(DatasetGraphOne.create(g.getModel().getGraph())) //
				.addEndpoint(Operation.GSP_R, "") //
				.addEndpoint(Operation.GSP_R, "data") //
				.addEndpoint(Operation.Query, "query") //
				.addEndpoint(Operation.Query, "sparql") //
				.build();

		FusekiServer fusekiServer = FusekiServer.create() //
				.port(54321) //
				.loopback(true) //
				.contextPath("/graph") //
				.add("/gids", metadataService) //
				.build();

		fusekiServer.start();
		fusekiServer.logServer();

		for (final RDFObject m : model) {
			final Optional<RDFObject> o = service.lookupById("http://localhost:54321/graph/gids/sparql", m.getId());
			if (o.isEmpty()) {
				Assertions.fail("Not found " + m);
			} else {
				log.trace("Comparing");
				log.trace("{}", m);
				log.trace("{}", o.get());
				Assertions.assertEquals(m, o.get());
				Assertions.assertNotSame(m, o.get());
			}
		}

		fusekiServer.stop();
	}
}
