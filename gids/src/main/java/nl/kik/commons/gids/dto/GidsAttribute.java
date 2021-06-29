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

	@Override
	public GidsAttribute<V> project(Source key, ZonedDateTime date) {
		if (getValues().containsKey(key)) {
			return GidsAttribute.<V>builder() //
					.alternatives(key, getAll(key, date)) //
					.build();
		} else {
			return null;
		}
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
