package nl.kik.commons.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@ToString
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode
public abstract class Alternatives<K, V> implements Projectable<K, Alternatives<K, V>> {
	private final Map<K, V> values = new HashMap<K, V>();

	public V get() {
		Iterator<V> iterator = values.values().iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	public V get(K key) {
		return values.get(key);
	}

	public V get(@SuppressWarnings("unchecked") K... keys) {
		for (K key : keys) {
			if (values.containsKey(key)) {
				return values.get(key);
			}
		}
		return null;
	}

	public Set<K> getSources() {
		return new HashSet<K>(values.keySet());
	}

	public Set<K> getSources(V value) {
		return values.entrySet().stream() //
				.filter(e -> Objects.equals(value, e.getValue())) //
				.map(Map.Entry::getKey) //
				.collect(Collectors.toSet());
	}

	public abstract static class AlternativesBuilder<K, V, C extends Alternatives<K, V>, B extends AlternativesBuilder<K, V, C, B>> {
		protected final Map<K, V> values = new HashMap<K, V>();

		public B alternatives(Map<K, V> values) {
			values.forEach(this::alternative);
			return self();
		}

		public B alternative(K key, V value) {
			if (key != null && value != null) {
				this.values.put(key, value);
			}
			return self();
		}
	}

}
