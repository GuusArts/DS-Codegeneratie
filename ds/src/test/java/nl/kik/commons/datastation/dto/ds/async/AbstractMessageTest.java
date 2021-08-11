package nl.kik.commons.datastation.dto.ds.async;

import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractMessageTest {
	protected static final ZoneId ZONE = ZoneId.systemDefault();
	protected Request<String> request;
	protected Response<String> response;
	protected ErrorReport<String> error;
	protected List<Message<String>> messages;

	@BeforeEach
	void setUp() throws Exception {
		request = Request.<String>builder() //
				.id("urn:request") //
				.issuer("did:sender") //
				.from("did:sender") //
				.to(Collections.singletonList("did:recipient")) //
				.expiration(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.creation(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.threadId("urn:thread") //
				.replyUrl(new URL("http://example.com/service/reply")) //
				.body("Ping") //
				.build();
		response = Response.<String>builder() //
				.id("urn:response") //
				.issuer("did:recipient") //
				.to(Collections.singletonList("did:sender")) //
				.from("did:recipient") //
				.expiration(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.creation(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.threadId("urn:thread") //
				.body("Pong") //
				.build();
		error = ErrorReport.<String>builder() //
				.id("urn:error") //
				.issuer("did:recipient") //
				.to(Collections.singletonList("did:sender")) //
				.from("did:recipient") //
				.expiration(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.creation(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.validFrom(
						ZonedDateTime.of(2021, 1, 25, 0, 0, 0, 0, AbstractMessageTest.ZONE).toOffsetDateTime().toZonedDateTime()) //
				.threadId("urn:thread") //
				.body("Wrong") //
				.build();
		messages = List.of(request, response, error);
	}

}
