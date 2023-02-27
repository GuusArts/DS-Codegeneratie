package nl.kik.commons.datastation.dto.kikv;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nl.kik.commons.datastation.dto.ds.SPARQLResult;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
	@JsonIgnore
	private String messageId;
	@JsonIgnore
	private String queryId;
	private SPARQLResult result;

	@JsonProperty
	public String getId() {
		return messageId + "#" + queryId;
	}

	public abstract static class ResultBuilder<C extends Result, B extends ResultBuilder<C, B>> {
		@JsonProperty
		public B id(String id) {
			String[] split = StringUtils.trimToEmpty(id).split("#", 2);
			if (split.length != 2 || StringUtils.isBlank(split[0]) || StringUtils.isBlank(split[1])) {
				throw new IllegalArgumentException("id must have format <messageID>#<queryId>");
			}
			return messageId(split[0]).queryId(split[1]);
		}
	}
}
