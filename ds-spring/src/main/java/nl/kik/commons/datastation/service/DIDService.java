//package nl.kik.commons.datastation.service;
//
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.text.ParseException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import jakarta.annotation.PostConstruct;
//
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import com.nimbusds.jose.JOSEException;
//import com.nimbusds.jose.JWSVerifier;
//import com.nimbusds.jose.crypto.ECDSAVerifier;
//import com.nimbusds.jose.crypto.Ed25519Verifier;
//import com.nimbusds.jose.crypto.RSASSAVerifier;
//import com.nimbusds.jose.jwk.Curve;
//import com.nimbusds.jose.jwk.JWK;
//import com.nimbusds.jose.jwk.KeyType;
//import com.nimbusds.jose.jwk.OctetKeyPair;
//import com.nimbusds.jose.util.Base64URL;
//
//import foundation.identity.did.DIDDocument;
//import foundation.identity.did.PublicKey;
//import foundation.identity.did.jsonld.DIDKeywords;
//import foundation.identity.jsonld.JsonLDUtils;
//import uniresolver.ResolutionException;
//import uniresolver.driver.Driver;
//import uniresolver.local.LocalUniResolver;
//import uniresolver.result.ResolveResult;
//
//@Service
//public class DIDService {
//	public static final String ED25519_VERIFICATION_KEY2018 = "Ed25519VerificationKey2018";
//	public static final String JSON_WEB_KEY2020 = "JsonWebKey2020";
//	private static final Set<String> SUPPORTED_KEYS = Set.of(DIDService.JSON_WEB_KEY2020,
//			DIDService.ED25519_VERIFICATION_KEY2018);
//	private LocalUniResolver uniResolver;
//
//	private Map<String, String> resolveOptions;
//	@Value("${nl.kik.commons.datastation.did.web.enable:false}")
//	private final boolean enableDIDweb = true;
//	@Value("${nl.kik.commons.datastation.did.web.localhost:false}")
//	private final boolean allowLocalhost = true;
//
//	@Value("${nl.kik.commons.datastation.did.web.localhost.port:8280}")
//	private final int localhostPort = 8280;
//
//	/**
//	 * @param issuer
//	 * @return
//	 * @throws ResolutionException
//	 */
//	public ResolveResult getDID(final String issuer) throws ResolutionException {
//		return uniResolver.resolve(issuer, resolveOptions);
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<PublicKey> getPublicKeys(final DIDDocument document) {
//		return JsonLDUtils.jsonLdGetJsonArray(document.getJsonObject(), DIDKeywords.JSONLD_TERM_PUBLICKEY).stream() //
//				.map(x -> (Map<String, Object>) x) //
//				.map(this::publicKey) //
//				.collect(Collectors.toList());
//	}
//
//	public JWSVerifier getVerifier(final String issuer, final String keyId)
//			throws JOSEException, ResolutionException, ParseException {
//		final ResolveResult resolveResult = getDID(issuer);
//		if (resolveResult == null || resolveResult.getDidDocument() == null) {
//			throw new JOSEException("No DID document found");
//		}
//		final List<PublicKey> authentications = getPublicKeys(resolveResult.getDidDocument()).stream() //
//				.filter(k -> CollectionUtils.emptyIfNull(k.getTypes()).stream().anyMatch(DIDService.SUPPORTED_KEYS::contains)) //
//				.collect(Collectors.toList());
//		if (keyId == null) {
//			if (authentications.size() == 1) {
//				return toVerifier(authentications.get(0));
//			}
//			throw new JOSEException("No keyId provided and not exactly one key found");
//		}
//		try {
//			final URI keyURI = new URI(keyId);
//			final Optional<PublicKey> key = authentications.stream() //
//					.filter(k -> Objects.equals(k.getId(), keyURI)) //
//					.findFirst();
//			return toVerifier(key.get());
//		} catch (URISyntaxException | NoSuchElementException e) {
//		}
//		try {
//			final URI keyURI = new URI(issuer + "#" + keyId);
//			final Optional<PublicKey> key = authentications.stream().filter(k -> Objects.equals(k.getId(), keyURI))
//					.findFirst();
//			return toVerifier(key.get());
//		} catch (URISyntaxException | NoSuchElementException e) {
//		}
//		throw new JOSEException("Either keyId was not a valid URI or no key with the given id was found");
//	}
//
//	@PostConstruct
//	public void init() {
//		uniResolver = new LocalUniResolver();
//
//		final Map<String, Driver> drivers = new HashMap<>();
//
//		if (enableDIDweb) {
////			HttpDriver driver = new HttpDriver();
////			driver.setPattern("");
////			driver.setResolveUri("http://localhost:8081/1.0/identifiers/$1");
////			drivers.put("driver-web", driver);
//
//			final DIDWeb driver = new DIDWeb();
//			driver.setAllowLocalhost(allowLocalhost);
//			driver.setLocalhostPort(localhostPort);
//			drivers.put("driver-web", driver);
//		}
//
//		uniResolver.setDrivers(drivers);
//
//		resolveOptions = Map.of( //
//				"accept", "application/did+ld+json" //
//		);
//	}
//
//	public PublicKey publicKey(final Map<String, Object> values) {
//		final PublicKey result = new PublicKey();
//		values.entrySet().forEach(e -> result.setJsonObjectKeyValue(e.getKey(), e.getValue()));
//		return result;
//	}
//
//	private JWSVerifier toVerifier(final JWK key) throws JOSEException {
//		final JWK publicJWK = key.toPublicJWK();
//		if (publicJWK.getKeyType() == KeyType.EC) {
//			return new ECDSAVerifier(publicJWK.toECKey());
//		}
//		if (publicJWK.getKeyType() == KeyType.RSA) {
//			return new RSASSAVerifier(publicJWK.toRSAKey());
//		}
//		if (publicJWK.getKeyType() == KeyType.OKP) {
//			return new Ed25519Verifier(publicJWK.toOctetKeyPair());
//		}
//		throw new IllegalArgumentException("Unsupported key type");
//	}
//
//	private JWSVerifier toVerifier(final PublicKey publicKey) throws ParseException, JOSEException {
//		if (publicKey.getPublicKeyJwk() != null) {
//			return toVerifier(JWK.parse(publicKey.getPublicKeyJwk()));
//		}
//		if (CollectionUtils.emptyIfNull(publicKey.getTypes()).contains(DIDService.ED25519_VERIFICATION_KEY2018)) {
//			String key = null;
//			if (publicKey.getPublicKeyBase64() != null) {
//				key = publicKey.getPublicKeyBase64();
//			}
//			if (publicKey.getPublicKeyBase58() != null) {
//				key = Base64.encodeBase64String(Base58.decode(publicKey.getPublicKeyBase58()));
//			}
//			if (StringUtils.isNotBlank(key)) {
//				return toVerifier(
//						new OctetKeyPair.Builder(Curve.Ed25519, new Base64URL(key)).keyID(publicKey.getId().toString()).build());
//			}
//		}
//		throw new JOSEException("No key in a known encoding found");
//	}
//
//}
