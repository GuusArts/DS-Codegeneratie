package nl.kik.commons.datastation.json;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import nl.kik.commons.datastation.dto.ds.SPARQLResult;
import nl.kik.commons.datastation.service.ResultService;

public class SPARQLResultSerialization {
    private static final ResultService service = new ResultService();

    public static final class Serialize extends JsonSerializer<SPARQLResult> {
        @Override
        public void serialize(SPARQLResult value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            try {
                gen.writeObject(service.wrap(value));
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
    }

    public static final class Deserialize extends JsonDeserializer<SPARQLResult> {
        @SuppressWarnings("unchecked")
        @Override
        public SPARQLResult deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JacksonException {
            try {
                return service.unwrap((Map<String, Object>) p.readValueAs(Map.class));
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
    }

}
