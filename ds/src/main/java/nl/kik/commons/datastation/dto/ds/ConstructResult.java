package nl.kik.commons.datastation.dto.ds;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstructResult extends SPARQLResult {
	private Map<String, Object> data;
}
