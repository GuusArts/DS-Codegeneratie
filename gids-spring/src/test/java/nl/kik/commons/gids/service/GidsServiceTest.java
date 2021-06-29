package nl.kik.commons.gids.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.vocabulary.RDF;
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
import nl.kik.commons.gids.dto.GraphOrRemote;
import nl.kik.commons.gids.dto.Location;
import nl.kik.commons.gids.dto.Organisation;
import nl.kik.commons.gids.dto.Region;
import nl.kik.commons.gids.dto.Source;
import nl.kik.commons.service.RDFService;

@Slf4j
class GidsServiceTest {
	private static final int PORT = 54321;
	private static final String SERVER_URL = "http://localhost:" + PORT + "/graph/gids";
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

		location = Location.builder() //
				.name(GidsAttribute.<String>builder().alternative(Source.LRZA, "Britney Health, bv").build()) //
				.number(GidsAttribute.<String>builder().alternative(Source.LRZA, "1")
						.alternative(Source.TABELBEHEER, "2").build()) //
				.agb(GidsAttribute.<String>builder().alternative(Source.LRZA, "12345678").build()) //
				.address(GidsAttribute.<Address>builder().alternative(Source.LRZA, address).build()) //
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
				.kvk(GidsAttribute.<String>builder().alternative(Source.LRZA, "98765432").build()) //
				.location(List.of(GidsAttribute.<Location>builder().alternative(Source.LRZA, location).build())) //
				.deliveryMethod(GidsAttribute.<DeliveryMethod>builder()
						.alternative(Source.KIK_STARTER, DeliveryMethod.KIKStarter).build()) //
				.build();

		model = List.of(region, concessionaire, careOffice, organisation);
	}

	@Test
	void testSaveLocal() {
		final Graph<Model> g = getLoadedModel();
		RDFService.snapshot(g, true, null);
	}

	@Test
	void testSaveRemote() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		FusekiServer fusekiServer = startFuseki(g);
		try {
			for (final GidsObject m : model) {
				service.save(SERVER_URL, null, GidsAttribute.of(Source.LRZA, m));
			}
			for (final RDFObject m : model) {
				final Optional<GidsAttribute<GidsObject>> o = service.lookupById(SERVER_URL, null, m.getId());
				if (o.isEmpty()) {
					Assertions.fail("Not found " + m);
				} else {
					log.trace("Comparing");
					log.trace("{}", m);
					log.trace("{}", o.get());
					Assertions.assertTrue(o.get().isUnique());
					Assertions.assertEquals(m, o.get().getAny());
					Assertions.assertNotSame(m, o.get().getAny());
				}
			}
		} finally {
			fusekiServer.stop();
			fusekiServer.join();
		}
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

		assertEquals(address.getHouseNumber().getAny(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				lrza.getHouseNumber().getAny());
		assertEquals(address.getHouseLetter().getAny(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				lrza.getHouseLetter().getAny());
		assertNotEquals(address.getHouseLetter().getAny(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				tabelbeheer.getHouseLetter().getAny());

		assertNotNull(address.getTown());
		assertNotNull(address.getTown().getAny());
		assertNotNull(lrza.getTown());
		assertNotNull(lrza.getTown().getAny());
		assertNull(tabelbeheer.getTown());
	}

	@Test
	void testLoadLocal() {
		final Graph<Model> g = getLoadedModel();
		for (final GidsObject m : model) {
			final Optional<GidsAttribute<GidsObject>> o = service.lookupById(g, m.getId());
			if (o.isEmpty()) {
				Assertions.fail("Not found " + m);
			} else {
				log.trace("Comparing");
				log.trace("{}", m);
				log.trace("{}", o.get());
				Assertions.assertTrue(o.get().isUnique());
				Assertions.assertEquals(m, o.get().getAny());
				Assertions.assertNotSame(m, o.get().getAny());
			}
		}
	}

	@Test
	void testLoadRemote() {
		FusekiServer fusekiServer = startFuseki(getLoadedModel());
		try {
			for (final GidsObject m : model) {
				final Optional<GidsAttribute<GidsObject>> o = service.lookupById(SERVER_URL, null, m.getId());
				if (o.isEmpty()) {
					Assertions.fail("Not found " + m);
				} else {
					log.trace("Comparing");
					log.trace("{}", m);
					log.trace("{}", o.get());
					Assertions.assertTrue(o.get().isUnique());
					Assertions.assertEquals(m, o.get().getAny());
					Assertions.assertNotSame(m, o.get().getAny());
				}
			}
		} finally {
			fusekiServer.stop();
			fusekiServer.join();
		}
	}

	/**
	 * @return
	 */
	protected FusekiServer startFuseki(Graph<Model> g) {
		final DataService metadataService = DataService //
				.newBuilder(DatasetGraphOne.create(g.getModel().getGraph())) //
				.addEndpoint(Operation.GSP_R, "") //
				.addEndpoint(Operation.GSP_R, "data") //
				.addEndpoint(Operation.Query, "query") //
				.addEndpoint(Operation.Query, "sparql") //
				.build();
		final DataService uploadService = DataService //
				.newBuilder(DatasetGraphOne.create(g.getModel().getGraph())) //
				.addEndpoint(Operation.GSP_RW, "") //
				.addEndpoint(Operation.GSP_RW, "data") //
				.build();

		FusekiServer fusekiServer = FusekiServer.create() //
				.port(PORT) //
				.loopback(true) //
				.contextPath("/graph") //
				.add("/gids", metadataService) //
				.add("/upload/gids", uploadService) //
				.build();

		fusekiServer.start();
		fusekiServer.logServer();

		try {
			Thread.sleep(2000); // Wait for server to start
		} catch (InterruptedException e) {
		}
		return fusekiServer;
	}

	@Test
	void testQueryLocal() {
		final Graph<Model> g = getLoadedModel();
		testQueries(new GraphOrRemote(g));
	}

	/**
	 * @return
	 */
	protected Graph<Model> getLoadedModel() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		model.forEach(o -> service.save(g, GidsAttribute.of(Source.LRZA, o)));
		return g;
	}

	@Test
	void testQueryRemote() {
		FusekiServer fusekiServer = startFuseki(getLoadedModel());
		try {
			testQueries(new GraphOrRemote(SERVER_URL));
		} finally {
			fusekiServer.stop();
			fusekiServer.join();
		}
	}

	/**
	 * @param g
	 */
	protected void testQueries(GraphOrRemote g) {
		// Illegal query type
		assertThrows(IllegalArgumentException.class, () -> {
			Query query = new AskBuilder() //
					.addPrefix("", GidsService.Vocabulary.uri) //
					.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
					.addWhere("?test", GidsService.Vocabulary.address, "?a") //
					.addWhere("?a", GidsService.Vocabulary.houseNumber, "'2'") //
					.build();
			service.query(g, query, Organisation.class);
		});

		// Too many variables
		assertThrows(IllegalArgumentException.class, () -> {
			Query query = new SelectBuilder() //
					.addPrefix("", GidsService.Vocabulary.uri) //
					.setDistinct(true) //
					.addVar("?test") //
					.addVar("?a") //
					.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
					.addWhere("?test", GidsService.Vocabulary.address, "?a") //
					.addWhere("?a", GidsService.Vocabulary.houseNumber, "'2'") //
					.build();
			service.query(g, query, Organisation.class);
		});

		// Regular search
		Query query = new SelectBuilder() //
				.addPrefix("", GidsService.Vocabulary.uri) //
				.setDistinct(true) //
				.addVar("?test") //
				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
				.addWhere("?a", GidsService.Vocabulary.houseNumber, "'2'") //
				.build();

		List<GidsAttribute<Organisation>> organisations = service.query(g, query, Organisation.class);
		assertEquals(1, organisations.size());
		assertTrue(organisations.iterator().next().isUnique());
		assertEquals(organisation, organisations.iterator().next().getAny());

		// Without type filter
		organisations = service.query(g, query, null);
		assertEquals(1, organisations.size());
		assertTrue(organisations.iterator().next().isUnique());
		assertEquals(organisation, organisations.iterator().next().getAny());

		// Not found
		query = new SelectBuilder() //
				.addPrefix("", GidsService.Vocabulary.uri) //
				.setDistinct(true) //
				.addVar("?test") //
				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
				.addWhere("?a", GidsService.Vocabulary.houseNumber, "'3'") //
				.build();

		organisations = service.query(g, query, Organisation.class);
		assertEquals(0, organisations.size());

		// Property with two values
		query = new SelectBuilder() //
				.addPrefix("", GidsService.Vocabulary.uri) //
				.setDistinct(true) //
				.addVar("?test") //
				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
				.addWhere("?a", GidsService.Vocabulary.houseLetter, "'c'") //
				.build();

		organisations = service.query(g, query, null);
		assertEquals(1, organisations.size());
		assertTrue(organisations.iterator().next().isUnique());
		assertEquals(organisation, organisations.iterator().next().getAny());

		query = new SelectBuilder() //
				.addPrefix("", GidsService.Vocabulary.uri) //
				.setDistinct(true) //
				.addVar("?test") //
				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
				.addWhere("?a", GidsService.Vocabulary.houseLetter, "'C'") //
				.build();

		organisations = service.query(g, query, null);
		assertEquals(1, organisations.size());
		assertTrue(organisations.iterator().next().isUnique());
		assertEquals(organisation, organisations.iterator().next().getAny());

		query = new SelectBuilder() //
				.addPrefix("", GidsService.Vocabulary.uri) //
				.setDistinct(true) //
				.addVar("?test") //
				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
				.addWhere("?a", GidsService.Vocabulary.houseLetter, "'D'") //
				.build();

		organisations = service.query(g, query, Organisation.class);
		assertEquals(0, organisations.size());

		// Select all (this is not efficient!)
		query = new SelectBuilder() //
				.addPrefix("", GidsService.Vocabulary.uri) //
				.setDistinct(true) //
				.addVar("?s") //
				.addWhere("?s", RDF.type, "?t") //
				.build();

		organisations = service.query(g, query, Organisation.class);
		assertEquals(1, organisations.size());
		assertTrue(organisations.iterator().next().isUnique());
		assertEquals(organisation, organisations.iterator().next().getAny());
		List<GidsAttribute<Address>> addresses = service.query(g, query, Address.class); // Cannot look up non-roots
		assertEquals(0, addresses.size());
		List<GidsAttribute<GidsObject>> objects = service.query(g, query, GidsObject.class);
		assertEquals(4, objects.size()); // We save more, but only roots count
	}
}
