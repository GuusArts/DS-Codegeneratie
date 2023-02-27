package nl.kik.commons.datastation.json;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import nl.kik.commons.datastation.dto.kikv.ResultSet;

public class Base64EncodedObject {
    public static final class Serialize extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            String serialized = mapper.writeValueAsString(value);
            String encoded = Base64.encodeBase64String(serialized.getBytes());
            gen.writeString(encoded);
        }
    }

    private static abstract class Deserialize<T> extends JsonDeserializer<T> {
        public abstract Class<T> getClazz();

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            String encoded = p.getValueAsString();
            if (encoded == null) {
                return null;
            }
            String serialized = new String(Base64.decodeBase64(encoded));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(serialized, getClazz());
        }
    }

    public static final class DeserializeResultSet extends Deserialize<ResultSet> {
        @Override
        public Class<ResultSet> getClazz() {
            return ResultSet.class;
        }
    }

}
