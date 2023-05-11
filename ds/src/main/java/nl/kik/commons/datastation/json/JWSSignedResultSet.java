package nl.kik.commons.datastation.json;

import java.io.IOException;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nimbusds.jose.JWSObject;

import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.service.CryptoService;

public class JWSSignedResultSet {
	public static CryptoService validator;

	public static class Serialize extends JsonSerializer<ResultSet> {
		@Inject
		private CryptoService validator;

		@Override
		public void serialize(ResultSet value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (value == null) {
				gen.writeNull();
				return;
			}
			try {
				gen.writeString((validator == null ? JWSSignedResultSet.validator : validator).sign(value).serialize());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	public static class Deserialize extends JsonDeserializer<ResultSet> {
		@Inject
		private CryptoService validator;

		@Override
		public ResultSet deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
			String encoded = p.getValueAsString();
			if (encoded == null) {
				return null;
			}
			try {
				JWSObject object = JWSObject.parse(encoded);
				return (validator == null ? JWSSignedResultSet.validator : validator).validate(object);
			} catch (Exception e) {
				throw new JsonMappingException(p, "Failed parsing and validating JWS", e);
			}
		}
	}

}
