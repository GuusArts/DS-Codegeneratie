package nl.kik.datastation.dto.ds;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractDSTest {
	protected static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");
	protected Message<String> request, response, error;
	protected List<Message<String>> messages;

	@BeforeEach
	void setUp() throws Exception {
		request = Request.<String>builder() //
				.id("urn:request") //
				.from("did:sender") //
				.to("did:recipient") //
				.expiration(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime()) //
				.creation(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime()) //
				.threadId("urn:thread") //
				.replyUrl(new URL("http://example.com/service/reply")) //
				.body("Ping") //
				.build();
		response = Response.<String>builder() //
				.id("urn:response") //
				.to("did:sender") //
				.from("did:recipient") //
				.expiration(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime()) //
				.creation(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime()) //
				.threadId("urn:thread") //
				.body("Pong") //
				.build();
		error = Response.<String>builder() //
				.id("urn:error") //
				.to("did:sender") //
				.from("did:recipient") //
				.expiration(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime()) //
				.creation(ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, ZONE).toOffsetDateTime()) //
				.threadId("urn:thread") //
				.body("Wrong") //
				.build();
		messages = List.of(request, response, error);
	}

}
