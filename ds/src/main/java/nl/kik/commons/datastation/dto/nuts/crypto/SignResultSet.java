package nl.kik.commons.datastation.dto.nuts.crypto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.json.Base64EncodedObject;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignResultSet extends SignJws<ResultSet> {
    @JsonSerialize(using = Base64EncodedObject.Serialize.class)
    @JsonDeserialize(using = Base64EncodedObject.DeserializeResultSet.class)
    private ResultSet payload;
}
