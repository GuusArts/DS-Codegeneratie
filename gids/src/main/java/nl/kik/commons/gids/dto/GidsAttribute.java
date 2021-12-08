package nl.kik.commons.gids.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Triple;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.Alternatives;
import nl.kik.commons.dto.Projectable;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class GidsAttribute<V> extends Alternatives<Source, V, GidsAttribute<V>>
		implements Comparable<GidsAttribute<V>> {
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

	@Override
	public int compareTo(final GidsAttribute<V> o) {
		if (o == null) {
			return 1;
		}
		if (o.getValues() == null) {
			return getValues() == null ? 0 : 1;
		}
		if (getValues() == null) {
			return -1;
		}
		if (getValues().size() < o.getValues().size()) {
			return 1;
		}
		if (getValues().size() > o.getValues().size()) {
			return -1;
		}
		final Set<Source> k1 = getValues().keySet();
		final Set<Source> k2 = o.getValues().keySet();
		if (k1.size() < k2.size()) {
			return 1;
		}
		if (k1.size() > k2.size()) {
			return -1;
		}
		final SortedSet<Source> s1 = new TreeSet<>(k1);
		if (!k1.equals(k2)) {
			final SortedSet<Source> s2 = new TreeSet<>(k2);
			s1.removeAll(k2);
			s2.removeAll(k1);
			// They are same size but not equal, so each has at least one element not in the
			// other
			return s1.first().compareTo(s2.first());
		}
		final Comparator<Triple<LocalDate, LocalDate, V>> c = new Comparator<Triple<LocalDate, LocalDate, V>>() {
			/**
			 * @param d1
			 * @param d2
			 */
			public int compare(final LocalDate d1, final LocalDate d2) {
				if (d1 == null) {
					return d2 == null ? 0 : 1;
				}
				if ((d2 == null) || d1.isBefore(d2)) {
					return -1;
				}
				if (d1.isAfter(d2)) {
					return 1;
				}
				return 0;
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public int compare(final Triple<LocalDate, LocalDate, V> o1, final Triple<LocalDate, LocalDate, V> o2) {
				if (o1 == null) {
					return o2 == null ? 0 : 1;
				}
				if (o2 == null) {
					return -1;
				}
				int result = compare(o1.getLeft(), o2.getLeft());
				if (result != 0) {
					return result;
				}
				result = compare(o1.getMiddle(), o2.getMiddle());
				if (result != 0) {
					return result;
				}
				if (o1.getRight() == null) {
					return o2.getRight() == null ? 0 : 1;
				}
				if (o2.getRight() == null) {
					return -1;
				}
				if (o1.getRight() instanceof Comparable && o2.getRight() instanceof Comparable) {
					return ((Comparable) o1.getRight()).compareTo(o2.getRight());
				}
				if (o1.getRight()instanceof final ZonedDateTime z1 && o2.getRight()instanceof final ZonedDateTime z2) {
					if (z1.isBefore(z2)) {
						return -1;
					}
					if (z1.isAfter(z2)) {
						return 1;
					}
				}
				return 0;
			}

		};
		for (final Source k : s1) {
			final Collection<Triple<LocalDate, LocalDate, V>> l1 = getValues().get(k);
			final Collection<Triple<LocalDate, LocalDate, V>> l2 = o.getValues().get(k);
			if (l1.size() < l2.size()) {
				return 1;
			}
			if (l1.size() > l2.size()) {
				return -1;
			}
			final List<Triple<LocalDate, LocalDate, V>> sl1 = l1.stream().sorted(c).toList();
			final List<Triple<LocalDate, LocalDate, V>> sl2 = l2.stream().sorted(c).toList();
			for (int i = 0; i < sl1.size(); i++) {
				final int result = c.compare(sl1.get(i), sl2.get(i));
				if (result != 0) {
					return result;
				}
			}
		}
		return 0;
	}

	public GidsAttribute<V> orNull() {
		if (getValues() == null || getValues().isEmpty()) {
			return null;
		}
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public GidsAttribute<V> project(final Source key, final LocalDate date) {
		return GidsAttribute.<V>builder() //
				.alternatives(getAll(key, date).stream() //
						.map(v -> {
							if (v.getRight()instanceof final Projectable p) {
								return Triple.of(v.getLeft(), v.getMiddle(), (V) p.project(key, date));
							}
							return v;
						}) //
						.filter(Objects::nonNull) //
						.toList()) //
				.build();
	}
}
