package nl.kik.commons.datastation.service.nuts;

import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;

import foundation.identity.did.DIDDocument;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.dto.nuts.NutsDIDDocument;
import nl.kik.commons.datastation.dto.nuts.credential.PresentationVerificationResult;
import nl.kik.commons.datastation.dto.nuts.credential.VerifyVerifiablePresentation;
import nl.kik.commons.datastation.dto.nuts.crypto.SignResultSet;
import nl.kik.commons.datastation.dto.nuts.vdr.DIDResolutionResult;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
import nl.kik.commons.datastation.service.CryptoService;
import nl.kik.commons.datastation.util.FunctionWrapper;

@Slf4j
public class NutsCryptoService implements CryptoService {
	private NutsNode nuts;
	@Autowired
	private ObjectMapper mapper;

	@Getter
	@Setter
	private int ttl = 30;

	public NutsCryptoService(NutsNode nuts, ObjectMapper mapper) {
		super();
		this.nuts = nuts;
		this.mapper = mapper;
	}

	private final Map<String, Pair<NutsDIDDocument, Instant>> didcache = new HashMap<>();
	private final Map<String, Pair<ECPublicKey, Instant>> keycache = new HashMap<>();

	@Override
	public JWSObject sign(URI sender, ResultSet value) {
		URI keyId = getDID(sender.toString()).getKeyAgreementVerificationMethods().stream() //
				.map(k -> k.getId()) //
				.findFirst().orElseThrow();

		return nuts.signJws(SignResultSet.builder() //
				.detached(false) //
				.kid(keyId) //
				.payload(value) //
				.build()); //
	}

	@Override
	public ResultSet validate(JWSObject value) throws Exception {
		ECPublicKey key = getKey(value.getHeader().getKeyID());
		JWSVerifier verifier = new DefaultJWSVerifierFactory().createJWSVerifier(value.getHeader(), key);
		if (value.verify(verifier)) {
			return mapper.readValue(value.getPayload().toBytes(), ResultSet.class);
		}
		throw new IllegalArgumentException("Could not validate signature of input");
	}

	@Override
	public void check(VerifiablePresentation vp, ZonedDateTime at) throws Exception {
		PresentationVerificationResult result = nuts.verifyVP(VerifyVerifiablePresentation.builder() //
				.validAt(at == null ? ZonedDateTime.now() : at) //
				.verifiablePresentation(vp) //
				.verifyCredentials(true) //
				.build());
		if (!result.isValidity()) {
			throw new IllegalArgumentException("Invalid VP " + result.getMessage());
		}
	}

	private ECPublicKey getKey(String kid) {
		return getViaCache(keycache, "key", kid, k -> getDID(didPart(k)).getKeyAgreementVerificationMethods().stream() //
				.filter(key -> key.getId().toString().equals(k)) //
				.map(key -> key.getPublicKeyJwk()) //
				.findFirst() //
				.map(FunctionWrapper.<Map<String, Object>, JWK, ParseException>wrapper(JWK::parse)) //
				.map(JWK::toECKey) //
				.map(FunctionWrapper.<ECKey, ECPublicKey, JOSEException>wrapper(ECKey::toECPublicKey)));
	}

	private String didPart(String kid) {
		return StringUtils.split(kid, '#')[0];
	}

	private DIDDocument getDID(String did) {
		return getViaCache(didcache, "DID", did,
				k -> Optional.of(nuts.resolveDID(k)).map(DIDResolutionResult::getDocument));
	}

	private synchronized <T> T getViaCache(Map<String, Pair<T, Instant>> cache, String name, String key,
			Function<String, Optional<T>> lookup) {
		log.trace("Looking up {} {}", name, key);
		Pair<T, Instant> cached = cache.get(key);
		if (cached != null) {
			if (cached.getRight().isAfter(Instant.now().minusSeconds(ttl))) {
				log.trace("Resolved via cache");
				return cached.getLeft();
			}
		}

		log.trace("Computing {} {}", name, key);
		Optional<T> resolveDID = lookup.apply(key);
		resolveDID.ifPresent(i -> cache.put(key, Pair.of(i, Instant.now())));
		log.trace("Computed {} {}", name, key);
		return resolveDID.orElse(null);
	}

	private synchronized <T> void cleanup(Map<String, Pair<T, Instant>> cache, String name) {
		log.trace("Cleaning up {}s", name);
		int before = cache.size();

		Instant grace = Instant.now().minusSeconds(ttl);

		cache.entrySet().stream() //
				.filter(e -> e.getValue().getRight().isBefore(grace)) //
				.map(e -> e.getKey()) //
				.toList().forEach(cache::remove);

		int after = cache.size();
		if (before > after) {
			log.info("Cleaned up {} {}(s); now storing {}", before - after, name, after);
		}
	}

	@Scheduled(fixedDelay = 60_000)
	public void cleanup() {
		cleanup(didcache, "DID");
		cleanup(keycache, "DID");
	}

}
