package nl.kik.commons.dto;

public interface Projectable<K, V> {
	V project(K key);
}
