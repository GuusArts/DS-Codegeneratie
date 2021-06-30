package nl.kik.commons.gids.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.Alternatives;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class GidsAttribute<V> extends Alternatives<Source, V, GidsAttribute<V>> {
	public static <V> GidsAttribute<V> of(Source s, V value) {
		return GidsAttribute.<V>builder() //
				.alternative(s, value) //
				.build();
	}

	public static <V> GidsAttribute<V> of(Source s, ZonedDateTime from, ZonedDateTime to, V value) {
		return GidsAttribute.<V>builder() //
				.alternative(s, from, to, value) //
				.build();
	}

	@Override
	public GidsAttribute<V> project(Source key, ZonedDateTime date) {
		return GidsAttribute.<V>builder() //
			.alternatives(key, getAll(key, date)) //
			.build();
	}

	private static final class GidsAttributeBuilderImpl<V>
			extends GidsAttribute.GidsAttributeBuilder<V, GidsAttribute<V>, GidsAttributeBuilderImpl<V>> {
		@java.lang.Override
		public GidsAttribute<V> build() {
			GidsAttribute<V> result = new GidsAttribute<V>(this);
			result.getValues().putAll(values);
			return result.orNull();
		}
	}

	public GidsAttribute<V> orNull() {
		if (getValues() == null || getValues().isEmpty()) {
			return null;
		} else {
			return this;
		}
	}
}
