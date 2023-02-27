package nl.kik.commons.datastation.json;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public class ListOrSingle {
	public static final class Serialize extends JsonSerializer<List<?>> {
		@Override
		public void serialize(List<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (CollectionUtils.isEmpty(value)) {
				gen.writeNull();
				return;
			}
			if (value.size() > 1) {
				gen.writeStartArray();
			}
			for (Object v : value) {
				if (v == null) {
					gen.writeNull();
				} else {
					gen.writeString(v.toString());
				}
			}
			if (value.size() > 1) {
				gen.writeEndArray();
			}
		}
	}

	private static abstract class Deserialize<T> extends JsonDeserializer<List<T>> {
		public abstract T convert(String value);

		@Override
		public List<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
			TreeNode tree = p.readValueAsTree();
			List<String> values = new ArrayList<>();
			if (tree instanceof ArrayNode a) {
				try {
					IteratorUtils.toList(a.elements()).forEach(element -> {
						if (element instanceof ValueNode v) {
							values.add(v.textValue());
						} else {
							throw new RuntimeException(
									new InvalidFormatException(p, "Expected string value", element, String.class));
						}
					});
				} catch (RuntimeException r) {
					if (r.getCause() instanceof InvalidFormatException e) {
						throw e;
					}
					throw r;
				}
			} else if (tree instanceof ValueNode v) {
				values.add(v.textValue());
			} else {
				throw new InvalidFormatException(p, "Expected string or list", tree, String.class);
			}
			return values.stream() //
					.map(vv -> vv == null ? null : convert(vv)) //
					.toList();
		}
	}

	public static final class DeserializeUUID extends Deserialize<UUID> {
		@Override
		public UUID convert(String value) {
			return UUID.fromString(value);
		}
	}

	public static final class DeserializeURI extends Deserialize<URI> {
		@Override
		public URI convert(String value) {
			return URI.create(value);
		}
	}

	public static final class DeserializeString extends Deserialize<String> {
		@Override
		public String convert(String value) {
			return value;
		}
	}
}
