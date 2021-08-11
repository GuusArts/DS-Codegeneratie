package nl.kik.commons.dto;

import java.time.LocalDate;

public interface Projectable<K, V> {
	default V project(K key) {
		return project(key, null);
	}

	default V project(LocalDate date) {
		return project(null, date);
	}

	V project(K key, LocalDate date);
}
