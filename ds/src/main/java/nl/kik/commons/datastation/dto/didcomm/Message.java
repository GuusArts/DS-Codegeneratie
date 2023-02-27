package nl.kik.commons.datastation.dto.didcomm;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.datastation.dto.kikv.Request;
import nl.kik.commons.datastation.dto.kikv.Response;
import nl.kik.commons.datastation.json.ListOrSingle;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ //
		@JsonSubTypes.Type(value = Request.class, name = "https://www.kik-v.nl/validated-query-request/1.0/request"), //
		@JsonSubTypes.Type(value = Response.class, name = "https://www.kik-v.nl/validated-query-request/1.0/response"), //
		@JsonSubTypes.Type(value = Error.class, name = "https://didcomm.org/report-problem/2.0/problem-report")//
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Message<T> {
	private UUID id, thid, pthid;
	private URI from;
	@Singular("singleTo")
	@JsonSerialize(using = ListOrSingle.Serialize.class)
	@JsonDeserialize(using = ListOrSingle.DeserializeURI.class)
	private List<URI> to;
	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
	private ZonedDateTime created_time;
	@JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
	private ZonedDateTime expires_time;
	private T body;
}
