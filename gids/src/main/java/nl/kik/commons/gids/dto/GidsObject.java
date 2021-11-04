package nl.kik.commons.gids.dto;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

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
	protected <V extends Comparable<V>> int compare(V v1, V v2) {
		if (v1 == null) {
			return v2 == null ? 0 : -1;
		}
		if (v2 == null) {
			return 1;
		}
		return v1.compareTo(v2);
	}

	protected <V extends Comparable<V>> int compare(List<V> l1, List<V> l2) {
		if (l1 == null) {
			return l2 == null ? 0 : -1;
		}
		if (l2 == null) {
			return 1;
		}
		if (l1.size() < l2.size()) {
			return 1;
		}
		if (l1.size() > l2.size()) {
			return -1;
		}
		List<V> sl1 = l1.stream().sorted().toList();
		List<V> sl2 = l2.stream().sorted().toList();
		for (int i = 0; i < sl1.size(); i++) {
			int result = sl1.get(i).compareTo(sl2.get(i));
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}

	protected <T> List<T> orNull(List<T> update) {
		if (update == null || update.isEmpty())
			return null;
		return update;
	}

}
