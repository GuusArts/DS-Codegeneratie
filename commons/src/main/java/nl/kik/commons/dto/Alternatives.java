package nl.kik.commons.dto;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@SuperBuilder(toBuilder = true)
@Getter
@ToString
@JsonInclude(Include.NON_NULL)
@Slf4j
@EqualsAndHashCode
public abstract class Alternatives<K, V, A extends Alternatives<K, V, A>> implements Projectable<K, A> {
	@JsonIgnore
	private final MultiValuedMap<K, Triple<LocalDate, LocalDate, V>> values = new ArrayListValuedHashMap<>();

	public Map<K, Collection<Triple<LocalDate, LocalDate, V>>> getValuesMap() {
		return values.asMap();
	}

	public void setValues(Map<K, Collection<Triple<LocalDate, LocalDate, V>>> values) {
		values.putAll(values);
	}

	public V getAny() {
		Iterator<Triple<LocalDate, LocalDate, V>> iterator = values.values().iterator();
		if (iterator.hasNext()) {
			return iterator.next().getRight();
		}
		return null;
	}

	public V getAny(K key) {
		return getAny(key, null);
	}

	public V getAny(LocalDate date) {
		return getAny(null, date);
	}

	public V get(K key, LocalDate date) {
		Objects.requireNonNull(key, "Key must be given to use get; otherwise guse getAny/getAll");
		Objects.requireNonNull(date, "Date must be given to use get; otherwise guse getAny/getAll");
		Collection<Triple<K, Pair<LocalDate, LocalDate>, V>> result = getAll(key, date);
		if (result.size() >= 2) {
			throw new InternalError("Somehow got more than one result; this indicates internal data corruption");
		}
		return result.stream().findFirst().map(Triple::getRight).orElse(null);
	}

	public V getAny(K key, LocalDate date) {
		LocalDate now = LocalDate.now();
		return getAll(key, date).stream() //
				.sorted(new CompareValues<K, V>(now)).findFirst() //
				.map(Triple::getRight) //
				.orElse(null);
	}

	public Collection<Triple<K, Pair<LocalDate, LocalDate>, V>> getAll(K key) {
		return getAll(key, null);
	}

	public Collection<Triple<K, Pair<LocalDate, LocalDate>, V>> getAll(LocalDate date) {
		return getAll(null, date);
	}

	/**
	 * @param key
	 * @param date
	 * @return
	 */
	public Collection<Triple<K, Pair<LocalDate, LocalDate>, V>> getAll(K key, LocalDate date) {
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

	private static boolean isActual(LocalDate from, LocalDate to, LocalDate date) {
		return (from == null || !from.isAfter(date)) && (to == null || to.isAfter(date));
	}

	public V getAny(@SuppressWarnings("unchecked") K... keys) {
		return getAny(null, keys);
	}

	public V getAny(LocalDate date, @SuppressWarnings("unchecked") K... keys) {
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

	static final class CompareValues<K, V> implements Comparator<Triple<K, Pair<LocalDate, LocalDate>, V>> {
		private final LocalDate now;

		CompareValues(LocalDate now) {
			this.now = now;
		}

		@Override
		public int compare(Triple<K, Pair<LocalDate, LocalDate>, V> s, Triple<K, Pair<LocalDate, LocalDate>, V> t) { // Sort by date with currently active preferred
			boolean actualS = s.getMiddle() == null || isActual(s.getMiddle().getLeft(), s.getMiddle().getRight(), now);
			boolean actualT = t.getMiddle() == null || isActual(t.getMiddle().getLeft(), t.getMiddle().getRight(), now);
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
		}
	}

	public abstract static class AlternativesBuilder<K, V, A extends Alternatives<K, V, A>, C extends Alternatives<K, V, A>, B extends AlternativesBuilder<K, V, A, C, B>> {
		protected final MultiValuedMap<K, Triple<LocalDate, LocalDate, V>> values = new ArrayListValuedHashMap<>();

		public B alternatives(MultiValuedMap<K, Triple<LocalDate, LocalDate, V>> values) {
			values.entries().forEach(
					e -> this.alternative(e.getKey(), e.getValue().getLeft(), e.getValue().getMiddle(), e.getValue().getRight()));
			return self();
		}

		public B alternatives(Map<K, Triple<LocalDate, LocalDate, V>> values) {
			values.forEach(this::alternative);
			return self();
		}

		public B alternatives(Collection<Triple<K, Pair<LocalDate, LocalDate>, V>> values) {
			values.forEach(v -> alternative(v.getLeft(), v.getMiddle() == null ? null : v.getMiddle().getLeft(),
					v.getMiddle() == null ? null : v.getMiddle().getRight(), v.getRight()));
			return self();
		}

		public B alternative(K key, LocalDate from, LocalDate to, V value) {
			return alternative(key, Triple.of(from, to, value));
		}

		public B alternative(K key, V value) {
			return alternative(key, null, null, value);
		}

		public B alternative(K key, Triple<LocalDate, LocalDate, V> value) {
			if (value != null && value.getLeft() != null && value.getMiddle() != null
					&& !value.getLeft().isBefore(value.getMiddle())) {
				throw new IllegalArgumentException("Value " + value + " has end date before or equal to start date");
			}
			if (key != null && value != null && value.getRight() != null) {
				List<Triple<LocalDate, LocalDate, V>> overlapping = this.values.get(key).stream() //
						.filter(v -> {
							if (v.getMiddle() != null && value.getLeft() != null && value.getLeft().isAfter(v.getMiddle())) {
								return false;
							}
							if (value.getMiddle() != null && v.getLeft() != null && v.getLeft().isAfter(value.getMiddle())) {
								return false;
							}
							return true;
						}) //
						.collect(Collectors.toList());
				log.debug("overlapping {}", overlapping);
				if (!overlapping.stream().allMatch(v -> Objects.equals(value.getRight(), v.getRight()))) {
					throw new IllegalArgumentException("Trying to add value " + value + " for source " + key
							+ " overlapping with incompatible values " + overlapping);
				}
				overlapping.forEach(v -> this.values.removeMapping(key, v));
				this.values.put(key,
						overlapping.isEmpty() ? value
								: Triple.of(extreme(overlapping, value, Triple::getLeft, LocalDate::compareTo), value.getMiddle(),
										value.getRight()));
			}
			return self();
		}
	}

	static <A, B, C, V> V extreme(List<Triple<A, B, C>> overlapping, Triple<A, B, C> extra,
			Function<Triple<A, B, C>, V> projection, Comparator<V> comparator) {
		List<V> values = overlapping.stream().map(projection).collect(Collectors.toList());
		V v = projection.apply(extra);
		if (v != null) {
			values.add(v);
		}
		if (values.stream().anyMatch(Objects::isNull)) {
			return null;
		}
		return values.stream().min(comparator).get();
	}

}
