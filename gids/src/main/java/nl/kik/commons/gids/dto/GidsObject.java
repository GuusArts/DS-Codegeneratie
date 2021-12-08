package nl.kik.commons.gids.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.commons.dto.RDFObject;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class GidsObject extends RDFObject {
	protected <V extends Comparable<V>> int compare(final List<V> l1, final List<V> l2) {
		if (l1 == null) {
			return l2 == null ? 0 : -1;
		}
		if ((l2 == null) || (l1.size() < l2.size())) {
			return 1;
		}
		if (l1.size() > l2.size()) {
			return -1;
		}
		final List<V> sl1 = l1.stream().sorted().toList();
		final List<V> sl2 = l2.stream().sorted().toList();
		for (int i = 0; i < sl1.size(); i++) {
			final int result = sl1.get(i).compareTo(sl2.get(i));
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}

	protected <V extends Comparable<V>> int compare(final V v1, final V v2) {
		if (v1 == null) {
			return v2 == null ? 0 : -1;
		}
		if (v2 == null) {
			return 1;
		}
		return v1.compareTo(v2);
	}

	protected <T> List<T> orNull(final List<T> update) {
		if (update == null || update.isEmpty()) {
			return null;
		}
		return update;
	}

}
