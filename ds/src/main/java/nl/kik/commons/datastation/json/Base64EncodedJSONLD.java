package nl.kik.commons.datastation.json;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialKeywords;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import foundation.identity.jsonld.JsonLDObject;
import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.kikv.ValidatedQueryCredential;

@Slf4j
public class Base64EncodedJSONLD {
	public static final class Serialize extends JsonSerializer<JsonLDObject> {
		@Override
		public void serialize(JsonLDObject value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			if (value == null) {
				gen.writeNull();
				return;
			}
			String serialized = value.toJson();
			String encoded = Base64.encodeBase64String(serialized.getBytes());
			gen.writeString(encoded);
		}
	}

	public static final class Deserialize extends JsonDeserializer<JsonLDObject> {

		@Override
		public JsonLDObject deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JacksonException {
			String encoded = p.getValueAsString();
			if (encoded == null) {
				return null;
			}
			String serialized = new String(Base64.decodeBase64(encoded));
			JsonLDObject jsonld = JsonLDObject.fromJson(serialized);
			if (jsonld.isType(ValidatedQueryCredential.TYPE)) {
				return ValidatedQueryCredential.fromJsonLDObject(jsonld);
			}
			if (jsonld.isType(VerifiableCredentialKeywords.JSONLD_TERM_VERIFIABLE_CREDENTIAL)) {
				return VerifiableCredential.fromJsonLDObject(jsonld);
			}
			if (jsonld.isType(VerifiableCredentialKeywords.JSONLD_TERM_VERIFIABLE_PRESENTATION)) {
				return VerifiablePresentation.fromJsonLDObject(jsonld);
			}
			log.warn("Received unknown base64 encoded jsonld {}", jsonld.getTypes());
			return jsonld;
		}
	}

}
