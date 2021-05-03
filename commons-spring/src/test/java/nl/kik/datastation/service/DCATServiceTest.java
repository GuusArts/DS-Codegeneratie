package nl.kik.datastation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.Graph;
import nl.kik.datastation.dto.RDFObject;
import nl.kik.datastation.dto.dcat.Catalog;
import nl.kik.datastation.dto.dcat.DataService;
import nl.kik.datastation.dto.dcat.Dataset;
import nl.kik.datastation.dto.dcat.Distribution;
import nl.kik.datastation.dto.dcat.kikv.Constants;
import nl.kik.datastation.dto.foaf.Agent;
import nl.kik.datastation.dto.foaf.Organization;

@Slf4j
public class DCATServiceTest {
	private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");
	private List<RDFObject> model;
	private Catalog catalog;
	private Dataset dataset;
	private Distribution distribution;
	private DataService dataservice, sparqlservice, graphstoreservice, shaclservice;
	private Agent publisher;

	private DCATService service;

	@BeforeEach
	void setUp() throws Exception {
		publisher = Organization.builder() //
				.name("Voorbeeldzorg") //
				.type(new URL("http://purl.org/adms/publishertype/NonProfitOrganisation")) //
				.build();
		dataservice = DataService.builder() //
				.conformsTo(Constants.STANDARD_VERIFIED_SPARQL) //
				.title("Gevalideerde vragen") //
				.description("Service voor het uitvoeren van gevalideerde vragen via sparql") //
				.endpointURL(new URL("http://data.example.com/api/verifiedsparql")).build();
		sparqlservice = DataService.builder() //
				.conformsTo(Constants.STANDARD_SPARQL) //
				.title("SPARQL") //
				.description("Service voor het uitvoeren van vragen via sparql") //
				.endpointURL(new URL("http://data.example.com/api/sparql")).build();
		graphstoreservice = DataService.builder() //
				.conformsTo(Constants.STANDARD_GRAPHSTORE) //
				.title("GRAPHSTORE") //
				.description("Service voor graph store") //
				.endpointURL(new URL("http://data.example.com/api/graphstore")).build();
		shaclservice = DataService.builder() //
				.conformsTo(Constants.STANDARD_SHACL) //
				.title("SHACL") //
				.description("Service voor shacl") //
				.endpointURL(new URL("http://data.example.com/api/shacl")).build();
		distribution = Distribution.builder() //
				.accessService(Set.of(dataservice, sparqlservice, graphstoreservice, shaclservice)) //
				.conformsTo(Constants.STANDARD_RDF) //
				.build();
		dataset = Dataset.builder() //
				.title("Linked data personeel") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.keyword(Set.of("Personeel")) //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) // Remove TS info
				.conformsTo(new URL("http://purl.org/ozo/hr")) //
				.accrualPeriodicity(Constants.FREQUENCY_DAILY) //
				.distribution(Set.of(distribution)) //
				.build();
		catalog = Catalog.builder() //
				.title("Datacatalogus voorbeeldzorg") //
				.description("Een beschrijving van de datasets in het datastation van voorbeeldzorg") //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) // Remove TS info
				.license(new URL("https://creativecommons.org/licenses/by/4.0/")) //
				.dataset(Set.of(dataset)) //
				.build();
		model = List.of(catalog, publisher, dataset, distribution, dataservice);

		service = new DCATService();
	}

	@Test
	void testSave() {
		Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (RDFObject m : model) {
			service.save(g, m);
		}
		RDFService.snapshot(g, true, null);
	}

	@Test
	void testLoad() {
		Graph<Model> g = Graph.create(ModelFactory.createDefaultModel());
		for (RDFObject m : model) {
			service.save(g, m);
		}
		for (RDFObject m : model) {
			Optional<RDFObject> o = service.lookupById(g, m.getId());
			if (o.isEmpty()) {
				fail("Not found " + m);
			} else {
				log.trace("Comparing");
				log.trace("{}", m);
				log.trace("{}", o.get());
				assertEquals(m, o.get());
			}
		}
	}
}
