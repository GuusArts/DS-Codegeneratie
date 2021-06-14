package nl.kik.commons.datastation.dto.dcat;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

import nl.kik.commons.datastation.dto.RDFObject;
import nl.kik.commons.datastation.dto.dcat.kikv.Constants;
import nl.kik.commons.datastation.dto.foaf.Agent;
import nl.kik.commons.datastation.dto.foaf.Organization;

public abstract class AbstractDCATTest {
	protected static final ZoneId ZONE = ZoneId.systemDefault();
	protected List<RDFObject> model;
	protected Catalog catalog;
	protected Dataset dataset;
	protected Distribution distribution;
	protected DataService dataservice, sparqlservice, sparqlservice2, graphstoreservice, shaclservice;
	protected Agent publisher;

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
		sparqlservice2 = DataService.builder() //
				.conformsTo(Constants.STANDARD_SPARQL) //
				.title("SPARQL 2") //
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
				.description("Service voor graph store") //
				.endpointURL(new URL("http://data.example.com/api/shacl")).build();
		distribution = Distribution.builder() //
				.accessService(Set.of(dataservice, sparqlservice, sparqlservice2, graphstoreservice, shaclservice)) //
				.conformsTo(Constants.STANDARD_RDF) //
				.build();
		dataset = Dataset.builder() //
				.title("Linked data personeel") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.keyword(Set.of("Personeel")) //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.conformsTo(new URL("http://purl.org/ozo/hr")) //
				.accrualPeriodicity(Constants.FREQUENCY_DAILY) //
				.distribution(Set.of(distribution)) //
				.build();
		catalog = Catalog.builder() //
				.title("Datacatalogus voorbeeldzorg") //
				.description("Een beschrijving van de datasets in het datastation van voorbeeldzorg") //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.license(new URL("https://creativecommons.org/licenses/by/4.0/")) //
				.dataset(Set.of(dataset)) //
				.build();
		model = List.of(catalog, publisher, dataset, distribution, dataservice);
	}

}
