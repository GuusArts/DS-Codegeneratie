package nl.kik.commons.dto;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.Triple;

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
public abstract class Alternatives<K, V, A extends Alternatives<K, V, A>>
		implements Projectable<K, Alternatives<K, V, A>> {
	private final MultiValuedMap<K, Triple<ZonedDateTime, ZonedDateTime, V>> values = new ArrayListValuedHashMap<>();

	public V getAny() {
		Iterator<Triple<ZonedDateTime, ZonedDateTime, V>> iterator = values.values().iterator();
		if (iterator.hasNext()) {
			return iterator.next().getRight();
		}
		return null;
	}

	public V getAny(K key) {
		return getAny(key, null);
	}

	public V getAny(ZonedDateTime date) {
		return getAny(null, date);
	}

	public V getAny(K key, ZonedDateTime date) {
		ZonedDateTime now = ZonedDateTime.now();
		return getAll(key, date).stream() //
				.sorted((s, t) -> { // Sort by date with currently active preferred
					boolean actualS = isActual(s, now);
					boolean actualT = isActual(t, now);
					if (actualS == actualT) {
						if (Objects.equals(s.getLeft(), t.getLeft())) {
							if (Objects.equals(s.getMiddle(), t.getMiddle())) {
								return 0;
							}
							if (s.getMiddle() == null) {
								return -1;
							}
							if (t.getMiddle() == null) {
								return 1;
							}
							return s.getMiddle().isBefore(t.getMiddle()) ? -1 : 1;
						}
						if (s.getLeft() == null) {
							return -1;
						}
						if (t.getLeft() == null) {
							return 1;
						}
						return s.getLeft().isBefore(t.getLeft()) ? -1 : 1;
					} else {
						return actualS ? -1 : 1;
					}
				}).findFirst() //
				.map(Triple::getRight) //
				.orElse(null);
	}

	public Collection<Triple<ZonedDateTime, ZonedDateTime, V>> getAll(K key) {
		return getAll(key, null);
	}	

	public Collection<Triple<ZonedDateTime, ZonedDateTime, V>> getAll(ZonedDateTime date) {
		return getAll(null, date);
	}	

	/**
	 * @param key
	 * @param date
	 * @return
	 */
	public Collection<Triple<ZonedDateTime, ZonedDateTime, V>> getAll(K key, ZonedDateTime date) {
		return (key == null? values.values().stream() : values.get(key).stream()) //
				.filter(t -> date == null || isActual(t, date))// if date != null, then it must be within period
				.collect(Collectors.toList());
	}

	public Collection<V> getAll() {
		return values.values().stream() //
				.map(t -> t.getRight()) //
				.collect(Collectors.toList());
	}

	private boolean isActual(Triple<ZonedDateTime, ZonedDateTime, V> t, ZonedDateTime date) {
		return (t.getLeft() == null || !t.getLeft().isAfter(date))
				&& (t.getMiddle() == null || t.getMiddle().isAfter(date));
	}

	public V getAny(@SuppressWarnings("unchecked") K... keys) {
		for (K key : keys) {
			if (values.containsKey(key)) {
				return getAny(key, null);
			}
		}
		return null;
	}

	public boolean isUnique() {
		return values.values().stream().map(Triple::getRight).collect(Collectors.toSet()).size() == 1;
	}

	public Set<K> getSources() {
		return new HashSet<K>(values.keySet());
	}

	public abstract static class AlternativesBuilder<K, V, A extends Alternatives<K, V, A>, C extends Alternatives<K, V, A>, B extends AlternativesBuilder<K, V, A, C, B>> {
		protected final Map<K, Triple<ZonedDateTime, ZonedDateTime, V>> values = new HashMap<>();

		public B alternatives(Map<K, Triple<ZonedDateTime, ZonedDateTime, V>> values) {
			values.forEach(this::alternative);
			return self();
		}

		public B alternatives(K key, Collection<Triple<ZonedDateTime, ZonedDateTime, V>> values) {
			values.forEach(v -> alternative(key, v));
			return self();
		}

		public B alternative(K key, ZonedDateTime from, ZonedDateTime to, V value) {
			return alternative(key, Triple.of(from, to, value));
		}

		public B alternative(K key, V value) {
			return alternative(key, null, null, value);
		}

		public B alternative(K key, Triple<ZonedDateTime, ZonedDateTime, V> value) {
			if (key != null && value != null && value.getRight() != null) {
				this.values.put(key, value);
			}
			return self();
		}
	}

}
