package nl.kik.commons.datastation.json;

import java.io.IOException;
import java.text.ParseException;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nimbusds.jose.JWSObject;

import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.service.ValidationService;

public class JWSSignedResultSet {
	public static class Serialize extends JsonSerializer<ResultSet> {
		@Inject
		private ValidationService validator;

		@Override
		public void serialize(ResultSet value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (value == null) {
				gen.writeNull();
				return;
			}
			try {
				gen.writeString(validator.sign(value).serialize());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	public static class Deserialize extends JsonDeserializer<ResultSet> {
		@Override
		public ResultSet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
			String encoded = p.getValueAsString();
			if (encoded == null) {
				return null;
			}
			try {
				JWSObject object = JWSObject.parse(encoded);
				ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(object.getPayload().toString(), ResultSet.class);
			} catch (ParseException e) {
				throw new JsonMappingException(p, "Failed parsing and validating JWS", e);
			}
		}
	}

}
