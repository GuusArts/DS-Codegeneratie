package nl.kik.commons.datastation.service;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;

public abstract class AbstractTokenService {
	protected <T> T checkEquals(final String name, final T expected, final T actual) throws ParseException {
		if (!Objects.equals(expected, actual))
			throw new ParseException(name + " does not match expectation (expected " + expected + ", got " + actual + ")", 0);
		return actual;
	}

	protected <T> T checkNonNull(final String name, final T value) throws ParseException {
		if (value == null)
			throw new ParseException("Required parameter " + name + " is absent", 0);
		return value;
	}

	protected <T> T getGeneric(final Map<String, Object> o, final String key, final Class<T> clazz)
			throws ParseException {
		if (o.get(key) == null)
			return null;
		final Object value = o.get(key);
		if (!clazz.isAssignableFrom(value.getClass()))
			throw new ParseException("Unexpected type of JSON object member with key \"" + key + "\"", 0);
		return clazz.cast(value);
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> getList(final Map<String, Object> o, final String key, final Class<T> clazz)
			throws ParseException {
		final List<Object> array = JSONObjectUtils.getJSONArray(o, key);
		if (array == null)
			return Collections.emptyList();
		if (!array.stream().allMatch(clazz::isInstance))
			throw new ParseException("Not all objects have type " + clazz, 0);
		return (List<T>) array;
	}

	protected Map<String, Object> getRequiredJSONObject(final Map<String, Object> json, final String key)
			throws ParseException {
		return checkNonNull(key, JSONObjectUtils.getJSONObject(json, key));
	}

	protected String getRequiredString(final JWTClaimsSet claims, final String key) throws ParseException {
		return checkNonNull(key, claims.getStringClaim(key));
	}

	protected String getRequiredString(final Map<String, Object> json, final String key) throws ParseException {
		return checkNonNull(key, JSONObjectUtils.getString(json, key));
	}

	protected String randomUUID() {
		return "urn:uuid:" + UUID.randomUUID();
	}

}
