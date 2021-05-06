package nl.kik.datastation.dto.vc;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractVerifiableCredentialTest {
	protected static final ZoneId ZONE = ZoneId.systemDefault();
	protected VerifiableCredential credential;
	protected VerifiablePresentation presentation;
	protected List<VerifiableBase> messages;

	@BeforeEach
	void setUp() throws Exception {
		credential = VerifiableCredential.builder() //
				.id("urn:credential") //
				.keyId("urn:key") //
				.from("did:sender") //
				.to("did:sender") //
				.expiration(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.creation(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.build();

		presentation = VerifiablePresentation.builder() //
				.id("urn:presentation") //
				.keyId("urn:key") //
				.from("did:authority") //
				.to("did:recipient") //
				.expiration(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.creation(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime().toZonedDateTime()) //
				.credential(credential) //
				.build();

		messages = List.of(credential, presentation);
	}

}
