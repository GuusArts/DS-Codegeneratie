package nl.kik.commons.datastation.dto.nuts.credential;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Visibility {
	Private("private"), Public("public");

	private String name;

	Visibility(String name) {
		this.name = name;
	}

	@JsonValue
	public String toString() {
		return name;
	}
}
