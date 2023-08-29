package nl.kik.commons.datastation.dto.dcat;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

import nl.kik.commons.datastation.dto.dcat.kikv.Constants;
import nl.kik.commons.datastation.dto.foaf.Agent;
import nl.kik.commons.datastation.dto.foaf.Organization;
import nl.kik.commons.dto.RDFObject;

public abstract class AbstractDCATTest {
	protected static final ZoneId ZONE = ZoneId.systemDefault();
	protected List<RDFObject> model;
	protected Catalog catalog;
	protected Dataset dataset1, dataset2, dataset3;
	protected CatalogRecord record1, record2;
	protected Agent publisher;

	@BeforeEach
	void setUp() throws Exception {
		publisher = Organization.builder() //
				.name("Voorbeeldzorg") //
				.type(new URI("http://purl.org/adms/publishertype/NonProfitOrganisation")) //
				.build();
		dataset1 = Dataset.builder() //
				.id("https://daas.example.com/2021/ds1") //
				.title("Linked data personeel 1") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.keyword(Set.of("Personeel")) //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.conformsTo(Set.of(new URI("http://purl.org/ozo/hr#2.0.0"))) //
				.accrualPeriodicity(Constants.FREQUENCY_DAILY) //
				.build();
		dataset2 = Dataset.builder() //
				.id("https://daas.example.com/2022/ds2") //
				.title("Linked data personeel 2") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.keyword(Set.of("Personeel")) //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2022, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.conformsTo(Set.of(new URI("http://purl.org/ozo/hr#2.0.0"))) //
				.accrualPeriodicity(Constants.FREQUENCY_DAILY) //
				.build();
		dataset3 = Dataset.builder() //
				.id("https://daas.example.com/2022/ds3") //
				.title("Linked data personeel 3") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.keyword(Set.of("Personeel")) //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2022, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.conformsTo(Set.of(new URI("http://purl.org/ozo/hr#2.0.1"))) //
				.accrualPeriodicity(Constants.FREQUENCY_DAILY) //
				.build();

		record1 = CatalogRecord.builder() //
				.id("https://daas.example.com/2021/r1") //
				.title("Linked data personeel record") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.modified(ZonedDateTime.of(2022, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.primaryTopic(dataset2).build();
		record2 = CatalogRecord.builder() //
				.id("https://daas.example.com/2021/r2") //
				.title("Linked data personeel record") //
				.description("Deze dataset bevat alle personeelsleden van voorbeeldzorg") //
				.issued(ZonedDateTime.of(2022, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.modified(ZonedDateTime.of(2022, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.build();

		catalog = Catalog.builder() //
				.title("Datacatalogus voorbeeldzorg") //
				.description("Een beschrijving van de datasets in het datastation van voorbeeldzorg") //
				.publisher(publisher) //
				.issued(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractDCATTest.ZONE).toOffsetDateTime()
						.toZonedDateTime()) //
				.license(new URI("https://creativecommons.org/licenses/by/4.0/")) //
				.dataset(Set.of(dataset1, dataset2, dataset3)) //
				.record(Set.of(record1, record2)) //
				.build();
		model = List.of(catalog, publisher, dataset1, dataset2, record2);
	}

}
