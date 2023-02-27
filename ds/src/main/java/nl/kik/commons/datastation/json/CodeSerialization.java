package nl.kik.commons.datastation.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.apicatalog.jsonld.StringUtils;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import nl.kik.commons.datastation.dto.didcomm.Code;
import nl.kik.commons.datastation.dto.didcomm.Scope;
import nl.kik.commons.datastation.dto.didcomm.Sorter;

public class CodeSerialization {
	public static final class Serialize extends JsonSerializer<Code> {
		@Override
		public void serialize(Code value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (value == null) {
				gen.writeNull();
				return;
			}
			Sorter sorter = Objects.requireNonNullElse(value.getSorter(), Sorter.Error);
			Scope scope = Objects.requireNonNullElse(value.getScope(), Scope.Protocol);

			StringBuilder sb = new StringBuilder();

			switch (sorter) {
			case Warning:
				sb.append('w');
				break;
			case Error:
			default:
				sb.append('e');
				break;
			}
			sb.append('.');

			switch (scope) {
			case Message:
				sb.append('m');
				break;
			case Protocol:
				sb.append('p');
				break;
			case Other:
			default:
				sb.append(Objects.requireNonNull(value.getOtherScope()));
				break;
			}
			sb.append('.');

			if (CollectionUtils.isEmpty(value.getDescriptors())) {
				throw new IllegalArgumentException("Expecting at least one descriptor");
			}
			sb.append(value.getDescriptors().stream().collect(Collectors.joining(".")));

			gen.writeString(sb.toString());
		}
	}

	public static final class Deserialize extends JsonDeserializer<Code> {

		@Override
		public Code deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
			String encoded = p.getValueAsString();
			if (encoded == null) {
				return null;
			}
			String[] split = encoded.split("[.]", 3);
			if (split.length != 3 || StringUtils.isBlank(split[0]) || StringUtils.isBlank(split[1])
					|| StringUtils.isBlank(split[2])) {
				throw new IllegalArgumentException("Expecting code with at least sorter, scope, and one descriptor");
			}

			var builder = Code.builder();

			switch (split[0]) {
			case "e":
				builder = builder.sorter(Sorter.Error);
				break;
			case "w":
				builder = builder.sorter(Sorter.Warning);
				break;
			default:
				throw new IllegalArgumentException("Expecting sorter to be 'e' or 'w'; was " + split[0]);
			}

			switch (split[1]) {
			case "p":
				builder = builder.scope(Scope.Protocol);
				break;
			case "m":
				builder = builder.scope(Scope.Message);
				break;
			default:
				builder = builder.scope(Scope.Other).otherScope(split[1]);
				break;
			}

			builder = builder.descriptors(Arrays.asList(split[2].split("[.]")));

			return builder.build();
		}
	}

}
