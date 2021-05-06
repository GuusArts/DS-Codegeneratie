package nl.kik.datastation.service;

import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.JWTClaimsSet;

import net.minidev.json.JSONObject;

public abstract class AbstractTokenService {
	protected static final String PROTECTED = "protected";
	protected static final String PAYLOAD = "payload";

	public JSONObject serialize(JOSEObject o) {
		return new JSONObject(Map.of( //
				PROTECTED, o.getHeader().toJSONObject(), //
				PAYLOAD, o.getPayload().toJSONObject() //
		));
	}

	protected <T> T checkEquals(String name, T expected, T actual) throws ParseException {
		if (!Objects.equals(expected, actual)) {
			throw new ParseException(
					name + " does not match expectation (expected " + expected + ", got " + actual + ")", 0);
		}
		return actual;
	}

	protected <T> T checkNonNull(String name, T value) throws ParseException {
		if (value == null) {
			throw new ParseException("Required parameter " + name + " is absent", 0);
		}
		return value;
	}

	protected JSONObject getRequiredJSONObject(JSONObject json, String key) throws ParseException {
		return checkNonNull(key, JSONObjectUtils.getJSONObject(json, key));
	}

	protected String getRequiredString(JWTClaimsSet claims, String key) throws ParseException {
		return checkNonNull(key, claims.getStringClaim(key));
	}

	protected String randomUUID() {
		return "urn:uuid:" + UUID.randomUUID();
	}

}
