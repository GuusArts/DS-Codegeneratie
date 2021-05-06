package nl.kik.datastation.service;

import java.time.OffsetDateTime;
import java.util.Date;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import nl.kik.datastation.dto.vc.VerifiableBase;

public class VerifiableCredentialService extends AbstractTokenService {
	public <T> JWSObject wrap(VerifiableBase m) {
		m = fillDefaults(m);
		JWSHeader header = new JWSHeader(JWSAlgorithm.EdDSA, JOSEObjectType.JWT, null, null, null, null, null, null,
				null, null, m.getKeyId(), true, null, null);

		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder() //
				.jwtID(m.getId()) //
				.issuer(m.getFrom()) //
				.audience(m.getTo()) //
				.notBeforeTime(m.getValidFrom() == null ? null : Date.from(m.getValidFrom().toInstant())) //
				.expirationTime(m.getExpiration() == null ? null : Date.from(m.getExpiration().toInstant())) //
				.issueTime(m.getCreation() == null ? null : Date.from(m.getCreation().toInstant())) //
		;
//		claims = wrap(claims, m);

		return new SignedJWT(header, claims.build());
	}

	@SuppressWarnings("unchecked")
	private <T extends VerifiableBase> T fillDefaults(T m) {
		return (T) m.toBuilder() //
				.id(m.getId() == null ? randomUUID() : m.getId()) //
				.creation(m.getCreation() == null ? OffsetDateTime.now().toZonedDateTime() : m.getCreation()) //
				.validFrom(m.getValidFrom() == null ? OffsetDateTime.now().toZonedDateTime() : m.getValidFrom()) //
				.build();
	}

}
