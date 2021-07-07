package nl.kik.commons.dto;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
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
public abstract class Alternatives<K, V, A extends Alternatives<K, V, A>> implements Projectable<K, A> {

	@JsonIgnore
	private final MultiValuedMap<K, Triple<ZonedDateTime, ZonedDateTime, V>> values = new ArrayListValuedHashMap<>();

	public Map<K, Collection<Triple<ZonedDateTime, ZonedDateTime, V>>> getValuesMap() {
		return values.asMap();
	}

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
					boolean actualS = s.getMiddle() == null
							|| isActual(s.getMiddle().getLeft(), s.getMiddle().getRight(), now);
					boolean actualT = t.getMiddle() == null
							|| isActual(t.getMiddle().getLeft(), t.getMiddle().getRight(), now);
					if (actualS == actualT) {
						if (Objects.equals(s.getMiddle(), t.getMiddle())) {
							return 0;
						}
						if (s.getMiddle() == null) {
							return 1;
						}
						if (t.getMiddle() == null) {
							return -1;
						}
						if (Objects.equals(s.getMiddle().getLeft(), t.getMiddle().getLeft())) {
							if (s.getMiddle().getRight() == null) {
								return -1;
							}
							if (t.getMiddle().getRight() == null) {
								return 1;
							}
							return s.getMiddle().getRight().isBefore(t.getMiddle().getRight()) ? -1 : 1;
						}
						if (s.getMiddle().getLeft() == null) {
							return -1;
						}
						if (t.getMiddle().getLeft() == null) {
							return 1;
						}
						return s.getMiddle().getLeft().isBefore(t.getMiddle().getLeft()) ? -1 : 1;
					} else {
						return actualS ? -1 : 1;
					}
				}).findFirst() //
				.map(Triple::getRight) //
				.orElse(null);
	}

	public Collection<Triple<K, Pair<ZonedDateTime, ZonedDateTime>, V>> getAll(K key) {
		return getAll(key, null);
	}

	public Collection<Triple<K, Pair<ZonedDateTime, ZonedDateTime>, V>> getAll(ZonedDateTime date) {
		return getAll(null, date);
	}

	/**
	 * @param key
	 * @param date
	 * @return
	 */
	public Collection<Triple<K, Pair<ZonedDateTime, ZonedDateTime>, V>> getAll(K key, ZonedDateTime date) {
		if (key == null) {
			return values.entries().stream() //
					.filter(e -> date == null || isActual(e.getValue().getLeft(), e.getValue().getMiddle(), date)) //
					.map(e -> Triple.of(e.getKey(), toPair(e.getValue()), e.getValue().getRight())) //
					.collect(Collectors.toList());
		} else {
			return values.get(key).stream() //
					.filter(v -> date == null || isActual(v.getLeft(), v.getMiddle(), date)) //
					.map(v -> Triple.of(key, toPair(v), v.getRight())) //
					.collect(Collectors.toList());
		}
	}

	private <L, R> Pair<L, R> toPair(Triple<L, R, ?> value) {
		if (value == null)
			return null;
		if (value.getLeft() == null && value.getMiddle() == null)
			return null;
		return Pair.of(value.getLeft(), value.getMiddle());
	}

	public Collection<V> getAll() {
		return values.values().stream() //
				.map(t -> t.getRight()) //
				.collect(Collectors.toList());
	}

	private boolean isActual(ZonedDateTime from, ZonedDateTime to, ZonedDateTime date) {
		return (from == null || !from.isAfter(date)) && (to == null || to.isAfter(date));
	}

	public V getAny(@SuppressWarnings("unchecked") K... keys) {
		return getAny(null, keys);
	}

	public V getAny(ZonedDateTime date, @SuppressWarnings("unchecked") K... keys) {
		for (K key : keys) {
			if (values.containsKey(key)) {
				return getAny(key, date);
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

		public B alternatives(Collection<Triple<K, Pair<ZonedDateTime, ZonedDateTime>, V>> values) {
			values.forEach(v -> alternative(v.getLeft(), v.getMiddle() == null ? null : v.getMiddle().getLeft(),
					v.getMiddle() == null ? null : v.getMiddle().getRight(), v.getRight()));
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
