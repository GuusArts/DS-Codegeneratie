package nl.kik.commons.datastation.dto.nuts.crypto;

import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SignJws<P> {
    private URI kid;
    @Singular
    private Map<String, String> headers;
    private boolean detached;
    
    public abstract P getPayload();
}
