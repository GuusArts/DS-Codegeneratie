package nl.kik.commons.dto;

import java.time.ZonedDateTime;

public interface Projectable<K, V> {
	default V project(K key) {
		return project(key, null);
	}

	default V project(ZonedDateTime date) {
		return project(null, date);
	}

	V project(K key, ZonedDateTime date);
}
