package nl.kik.datastation.dto.dcat;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.kik.datastation.dto.dcat.kikv.Constants;
import nl.kik.datastation.dto.foaf.Agent;
import nl.kik.datastation.dto.foaf.Organization;

class SetupDCATModelTest {
	private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");
	private List<Object> model;
	private Catalog catalog;
	private Dataset dataset;
	private Distribution distribution;
	private DataService dataservice, sparqlservice, graphstoreservice;
	private Agent publisher;

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
		graphstoreservice = DataService.builder() //
				.conformsTo(Constants.STANDARD_GRAPHSTORE) //
				.title("GRAPHSTORE") //
				.description("Service voor graph store") //
				.endpointURL(new URL("http://data.example.com/api/graphstore")).build();
		distribution = Distribution.builder() //
				.accessService(List.of(dataservice, sparqlservice, graphstoreservice)) //
				.conformsTo(Constants.STANDARD_RDF) //
				.build();
		dataset = Dataset.builder() //
				.title("Linked data personeel") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.keyword(List.of("Personeel")) //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE)) //
				.conformsTo(new URL("http://purl.org/ozo/hr")) //
				.accrualPeriodicity(Constants.FREQUENCY_DAILY) //
				.distribution(List.of(distribution)) //
				.build();
		catalog = Catalog.builder() //
				.title("Datacatalogus voorbeeldzorg") //
				.description("Een beschrijving van de datasets in het datastation van voorbeeldzorg") //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE)) //
				.license(new URL("https://creativecommons.org/licenses/by/4.0/")) //
				.dataset(List.of(dataset)) //
				.build();
		model = List.of(catalog, publisher, dataset, distribution, dataservice);
	}

	@Test
	void test() {
		System.out.println(model);
	}

}
