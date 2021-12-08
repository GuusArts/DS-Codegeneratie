package nl.kik.commons.gids.service;

import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import org.apache.jena.sparql.lang.sparql_11.ParseException;
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
	private static final String SERVER_URL = "http://localhost:" + GidsServiceTest.PORT + "/graph/gids";
	private GidsService service;
	private Address address;
	private CareOffice careOffice;
	private Region region;
	private Concessionaire concessionaire, concessionaire2;
	private Location location;
	private Organisation organisation;
	private List<GidsObject> model;

	/**
	 * @return
	 */
	protected Graph<Model> getLoadedModel() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		model.forEach(o -> service.save(g, GidsAttribute.of(Source.TABELBEHEER, o)));
		service.save(g, GidsAttribute.of(Source.LRZA, organisation));
		return g;
	}

	@BeforeEach
	void setUp() throws Exception {
		service = new GidsService();

		region = Region.builder() //
				.code(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "772").build()) //
				.build();

		concessionaire = Concessionaire.builder() //
				.name(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "CZ").build()) //
				.build();

		concessionaire2 = Concessionaire.builder() //
				.name(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "VGZ").build()) //
				.build();

		careOffice = CareOffice.builder() //
				.code(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "5529").build()) //
				.name(GidsAttribute.<String>builder().alternative(Source.TABELBEHEER, "ZUIDOOST-BRABANT").build()) //
				.concessionaire(GidsAttribute.<Concessionaire>builder().alternative(Source.TABELBEHEER, concessionaire).build()) //
				.region(GidsAttribute.<Region>builder().alternative(Source.TABELBEHEER, region).build()) //
				.build();

		address = Address.builder() //
				.houseNumber(GidsAttribute.<String>builder().alternative(Source.LRZA, "2").build()) //
				.houseLetter(GidsAttribute.<String>builder().alternative(Source.LRZA, "c").build()) //
				.town(GidsAttribute.<String>builder().alternative(Source.LRZA, "Eindhoven").build()) //
				.province(GidsAttribute.<String>builder().alternative(Source.LRZA, "Noord-Brabant").build()) //
				.postalcode(GidsAttribute.<String>builder().alternative(Source.LRZA, "5621KA").build()) //
				.street(GidsAttribute.<String>builder().alternative(Source.LRZA, "Suikerpeerstraat").build()) //
				.build();

		location = Location.builder() //
				.name(List.of(GidsAttribute.<String>builder().alternative(Source.LRZA, "Britney Health, bv").build())) //
				.number(GidsAttribute.<String>builder().alternative(Source.LRZA, "1").build()) //
				.agb(List.of(GidsAttribute.<String>builder().alternative(Source.LRZA, "12345678").build())) //
				.sbi(List.of(GidsAttribute.<String>builder().alternative(Source.LRZA, "871").build())) //
				.address(GidsAttribute.<Address>builder().alternative(Source.LRZA, address).build()) //
				.build();

		organisation = Organisation.builder() //
				.address(GidsAttribute.<Address>builder().alternative(Source.LRZA, address).build()) //
				.office(GidsAttribute.<CareOffice>builder().alternative(Source.LRZA, careOffice).build()) //
				.name(List.of(GidsAttribute.<String>builder().alternative(Source.LRZA, "Britney Health").build())) //
				.lastModified(GidsAttribute.<ZonedDateTime>builder()
						.alternative(Source.LRZA, ZonedDateTime.now().toOffsetDateTime().toZonedDateTime()).build()) //
				.agb(List.of(GidsAttribute.<String>builder().alternative(Source.LRZA, "23456789").build(),
						GidsAttribute.<String>builder().alternative(Source.LRZA, "34567890").build())) //
				.sbi(List.of(GidsAttribute.<String>builder().alternative(Source.LRZA, "871").build(),
						GidsAttribute.<String>builder().alternative(Source.LRZA, "88101").build())) //
				.kvk(GidsAttribute.<String>builder().alternative(Source.LRZA, "98765432").build()) //
				.location(List.of(GidsAttribute.<Location>builder().alternative(Source.LRZA, location).build())) //
				.build();

		model = List.of(region, concessionaire, concessionaire2, careOffice);
	}

	/**
	 * @return
	 */
	protected FusekiServer startFuseki(final Graph<Model> g) {
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

		final FusekiServer fusekiServer = FusekiServer.create() //
				.port(GidsServiceTest.PORT) //
				.loopback(true) //
				.contextPath("/graph") //
				.add("/gids", metadataService) //
				.add("/upload/gids", uploadService) //
				.build();

		fusekiServer.start();
		fusekiServer.logServer();

		try {
			Thread.sleep(2000); // Wait for server to start
		} catch (final InterruptedException e) {
		}
		return fusekiServer;
	}

	@Test
	void testLoadLocal() {
		final Graph<Model> g = getLoadedModel();
		for (final GidsObject m : model) {
			final Optional<GidsAttribute<GidsObject>> o = service.lookupById(g, m.getId());
			if (o.isEmpty()) {
				Assertions.fail("Not found " + m);
			} else {
				GidsServiceTest.log.info("Comparing");
				GidsServiceTest.log.info("{}", m);
				GidsServiceTest.log.info("{}", o.get());
				Assertions.assertTrue(o.get().isUnique());
				Assertions.assertEquals(m, o.get().getAny());
				Assertions.assertNotSame(m, o.get().getAny());
			}
		}
	}

	@Test
	void testLoadRemote() {
		final FusekiServer fusekiServer = startFuseki(getLoadedModel());
		try {
			for (final GidsObject m : model) {
				final Optional<GidsAttribute<GidsObject>> o = service.lookupById(GidsServiceTest.SERVER_URL, null, m.getId());
				if (o.isEmpty()) {
					Assertions.fail("Not found " + m);
				} else {
					GidsServiceTest.log.trace("Comparing");
					GidsServiceTest.log.trace("{}", m);
					GidsServiceTest.log.trace("{}", o.get());
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
	void testLookupById() throws ParseException {
		final Graph<Model> loadedModel = getLoadedModel();
		RDFService.emitTTL(loadedModel, Paths.get("output.ttl"));
		final FusekiServer fusekiServer = startFuseki(loadedModel);
		final GraphOrRemote g = new GraphOrRemote(GidsServiceTest.SERVER_URL);
		try {
			// Regular search
			Query query = new SelectBuilder() //
					.addPrefix("", GidsService.Vocabulary.uri) //
					.setDistinct(true) //
					.addVar("?test") //
					.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
					.addWhere("?test", GidsService.Vocabulary.agb, "'23456789'") //
					.build();

			List<GidsAttribute<Organisation>> organisations = service.query(g, query, Organisation.class);
			Assertions.assertEquals(1, organisations.size());
			Assertions.assertTrue(organisations.iterator().next().isUnique());
			Assertions.assertEquals(organisation, organisations.iterator().next().getAny());

			query = new SelectBuilder() //
					.addPrefix("", GidsService.Vocabulary.uri) //
					.addPrefix("gids:", "gids:") //
					.setDistinct(true) //
					.addVar("?test") //
					.addBind(organisations.get(0).getAny().getId(), "?test")
					.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
					.build();

			organisations = service.query(g, query, Organisation.class);
			Assertions.assertEquals(1, organisations.size());
			Assertions.assertTrue(organisations.iterator().next().isUnique());
			Assertions.assertEquals(organisation, organisations.iterator().next().getAny());
		} finally {
			fusekiServer.stop();
			fusekiServer.join();
		}
	}

	@Test
	void testProject() {
		final Address lrza = address.project(Source.LRZA);
		final Address tabelbeheer = address.project(Source.TABELBEHEER);

		GidsServiceTest.log.trace("Projecting");
		GidsServiceTest.log.trace("{}", address);
		GidsServiceTest.log.trace("{}", lrza);
		GidsServiceTest.log.trace("{}", tabelbeheer);

		Assertions.assertEquals(address, lrza);
		Assertions.assertNull(tabelbeheer);

		Assertions.assertEquals(address.getHouseNumber().getAny(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				lrza.getHouseNumber().getAny());
		Assertions.assertEquals(address.getHouseLetter().getAny(Source.UNKNOWN, Source.LRZA, Source.TABELBEHEER),
				lrza.getHouseLetter().getAny());

		Assertions.assertNotNull(address.getTown());
		Assertions.assertNotNull(address.getTown().getAny());
		Assertions.assertNotNull(lrza.getTown());
		Assertions.assertNotNull(lrza.getTown().getAny());
	}

	/**
	 * @param g
	 * @throws ParseException
	 */
	protected void testQueries(final GraphOrRemote g) throws ParseException {
		// Illegal query type
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Query query = new AskBuilder() //
					.addPrefix("", GidsService.Vocabulary.uri) //
					.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
					.addWhere("?test", GidsService.Vocabulary.address, "?a") //
					.addWhere("?a", GidsService.Vocabulary.houseNumber, "'2'") //
					.build();
			service.query(g, query, Organisation.class);
		});

		// Too many variables
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			final Query query = new SelectBuilder() //
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
		Assertions.assertEquals(1, organisations.size());
		Assertions.assertTrue(organisations.iterator().next().isUnique());
		Assertions.assertEquals(organisation, organisations.iterator().next().getAny());

		// Without type filter
		organisations = service.query(g, query, null);
		Assertions.assertEquals(1, organisations.size());
		Assertions.assertTrue(organisations.iterator().next().isUnique());
		Assertions.assertEquals(organisation, organisations.iterator().next().getAny());

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
		Assertions.assertEquals(0, organisations.size());

//		// Property with two values
//		query = new SelectBuilder() //
//				.addPrefix("", GidsService.Vocabulary.uri) //
//				.setDistinct(true) //
//				.addVar("?test") //
//				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
//				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
//				.addWhere("?a", GidsService.Vocabulary.houseLetter, "'c'") //
//				.build();
//
//		organisations = service.query(g, query, null);
//		Assertions.assertEquals(1, organisations.size());
//		Assertions.assertTrue(organisations.iterator().next().isUnique());
//		Assertions.assertEquals(organisation, organisations.iterator().next().getAny());
//
//		query = new SelectBuilder() //
//				.addPrefix("", GidsService.Vocabulary.uri) //
//				.setDistinct(true) //
//				.addVar("?test") //
//				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
//				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
//				.addWhere("?a", GidsService.Vocabulary.houseLetter, "'C'") //
//				.build();
//
//		organisations = service.query(g, query, null);
//		Assertions.assertEquals(1, organisations.size());
//		Assertions.assertTrue(organisations.iterator().next().isUnique());
//		Assertions.assertEquals(organisation, organisations.iterator().next().getAny());
//
//		query = new SelectBuilder() //
//				.addPrefix("", GidsService.Vocabulary.uri) //
//				.setDistinct(true) //
//				.addVar("?test") //
//				.addWhere("?test", RDF.type, GidsService.Vocabulary.Organisation) //
//				.addWhere("?test", GidsService.Vocabulary.address, "?a") //
//				.addWhere("?a", GidsService.Vocabulary.houseLetter, "'D'") //
//				.build();
//
//		organisations = service.query(g, query, Organisation.class);
//		Assertions.assertEquals(0, organisations.size());

		// Select all (this is not efficient!)
		query = new SelectBuilder() //
				.addPrefix("", GidsService.Vocabulary.uri) //
				.setDistinct(true) //
				.addVar("?s") //
				.addWhere("?s", RDF.type, "?t") //
				.build();

		organisations = service.query(g, query, Organisation.class);
		Assertions.assertEquals(1, organisations.size());
		Assertions.assertTrue(organisations.iterator().next().isUnique());
		Assertions.assertEquals(organisation, organisations.iterator().next().getAny());
		final List<GidsAttribute<Address>> addresses = service.query(g, query, Address.class); // Cannot look up
		// non-roots
		Assertions.assertEquals(0, addresses.size());
		final List<GidsAttribute<GidsObject>> objects = service.query(g, query, GidsObject.class);
		Assertions.assertEquals(5, objects.size()); // We save more, but only roots count
	}

	@Test
	void testQueryLocal() throws ParseException {
		final Graph<Model> g = getLoadedModel();
		testQueries(new GraphOrRemote(g));
	}

	@Test
	void testQueryRemote() throws ParseException {
		final FusekiServer fusekiServer = startFuseki(getLoadedModel());
		try {
			testQueries(new GraphOrRemote(GidsServiceTest.SERVER_URL));
		} finally {
			fusekiServer.stop();
			fusekiServer.join();
		}
	}

	@Test
	void testSaveLocal() {
		final Graph<Model> g = getLoadedModel();
		RDFService.snapshot(g, true, null);
	}

	@Test
	void testSaveRemote() {
		final Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		final FusekiServer fusekiServer = startFuseki(g);
		try {
			for (final GidsObject m : model) {
				service.save(GidsServiceTest.SERVER_URL, null, GidsAttribute.of(Source.TABELBEHEER, m));
			}
			service.save(GidsServiceTest.SERVER_URL, null, GidsAttribute.of(Source.LRZA, organisation));

			final List<GidsObject> all = new ArrayList<>(model);
			all.add(organisation);
			for (final RDFObject m : all) {
				final Optional<GidsAttribute<GidsObject>> o = service.lookupById(GidsServiceTest.SERVER_URL, null, m.getId());
				if (o.isEmpty()) {
					Assertions.fail("Not found " + m);
				} else {
					GidsServiceTest.log.trace("Comparing");
					GidsServiceTest.log.trace("{}", m);
					GidsServiceTest.log.trace("{}", o.get());
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
}
