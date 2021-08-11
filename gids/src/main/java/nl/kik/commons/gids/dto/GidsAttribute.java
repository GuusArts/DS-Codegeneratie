package nl.kik.commons.gids.dto;

import java.time.LocalDate;

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
	private static final class GidsAttributeBuilderImpl<V>
			extends GidsAttribute.GidsAttributeBuilder<V, GidsAttribute<V>, GidsAttributeBuilderImpl<V>> {
		@java.lang.Override
		public GidsAttribute<V> build() {
			final GidsAttribute<V> result = new GidsAttribute<>(this);
			result.getValues().putAll(values);
			return result.orNull();
		}
	}

	public static <V> GidsAttribute<V> of(final Source s, final LocalDate from, final LocalDate to, final V value) {
		return GidsAttribute.<V>builder() //
				.alternative(s, from, to, value) //
				.build();
	}

	public static <V> GidsAttribute<V> of(final Source s, final V value) {
		return GidsAttribute.<V>builder() //
				.alternative(s, value) //
				.build();
	}

	public GidsAttribute<V> orNull() {
		if (getValues() == null || getValues().isEmpty())
			return null;
		return this;
	}

	@Override
	public GidsAttribute<V> project(final Source key, final LocalDate date) {
		return GidsAttribute.<V>builder() //
				.alternatives(getAll(key, date)) //
				.build();
	}
}
