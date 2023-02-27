package nl.kik.commons.datastation.dto.nuts.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccessToken {
    @Builder.Default
    private String grant_type = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private String assertion;
}
