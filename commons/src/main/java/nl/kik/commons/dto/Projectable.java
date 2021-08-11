package nl.kik.commons.dto;

import java.time.LocalDate;

public interface Projectable<K, V> {
	default V project(final K key) {
		return project(key, null);
	}

	V project(K key, LocalDate date);

	default V project(final LocalDate date) {
		return project(null, date);
	}
}
