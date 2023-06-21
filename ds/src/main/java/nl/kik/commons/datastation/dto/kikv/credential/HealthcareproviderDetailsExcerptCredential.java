package nl.kik.commons.datastation.dto.kikv.credential;

import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialContexts;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialKeywords;

import foundation.identity.jsonld.ConfigurableDocumentLoader;
import foundation.identity.jsonld.JsonLDObject;

/**
 * Pre-release; name as well as properties are not definite
 * 
 * @author michael
 *
 */
public class HealthcareproviderDetailsExcerptCredential extends VerifiableCredential {
	public static final String TYPE = "HealthcareproviderDetailsExcerptCredential";
	public static final URI URL = URI.create("https://kik-v.nl/provider/v1.json");

	public static final String ORGANIZATION = "organization";
	public static final String KVK = "chamberOfCommerceNumber";
	public static final String NAME = "name";

	public static final URI[] DEFAULT_JSONLD_CONTEXTS = {
			VerifiableCredentialContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_V1, URL };
	public static final String[] DEFAULT_JSONLD_TYPES = {
			VerifiableCredentialKeywords.JSONLD_TERM_VERIFIABLE_CREDENTIAL, TYPE };
	public static final String DEFAULT_JSONLD_PREDICATE = VerifiableCredentialKeywords.JSONLD_TERM_VERIFIABLECREDENTIAL;
	public static final DocumentLoader DEFAULT_DOCUMENT_LOADER;
	public static final Map<URI, JsonDocument> CONTEXTS;

	static {
		try {
			CONTEXTS = new HashMap<>();
			CONTEXTS.putAll(VerifiableCredentialContexts.CONTEXTS);

			CONTEXTS.put(URL, JsonDocument.of(MediaType.JSON_LD, HealthcareproviderDetailsExcerptCredential.class
					.getResourceAsStream("HealthcareproviderDetailsExcerptCredential.ldjson")));

			for (Map.Entry<URI, JsonDocument> context : CONTEXTS.entrySet()) {
				context.getValue().setDocumentUrl(context.getKey());
			}
		} catch (JsonLdError ex) {
			throw new ExceptionInInitializerError(ex);
		}

		DEFAULT_DOCUMENT_LOADER = new ConfigurableDocumentLoader(CONTEXTS);
	}

	public static class Builder<B extends Builder<B>> extends VerifiableCredential.Builder<B> {
		private Map<String, Object> claims;
		private URI subjectId;
		private Map<String, Object> organization;

		public Builder(HealthcareproviderDetailsExcerptCredential jsonLdObject) {
			super(jsonLdObject);
		}

		@Override
		public HealthcareproviderDetailsExcerptCredential build() {
			super.build();

			if (MapUtils.isNotEmpty(claims) || subjectId != null) {
				CredentialSubject credentialSubject = CredentialSubject.builder() //
						.id(subjectId) //
						.claims(claims) //
						.build();
				credentialSubject.addToJsonLDObject(this.jsonLdObject);
			}

			return (HealthcareproviderDetailsExcerptCredential) this.jsonLdObject;
		}

		@SuppressWarnings("unchecked")
		public B claims(Map<String, Object> claims) {
			this.claims = claims;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B subjectId(URI subjectId) {
			ensureClaims();
			this.subjectId = subjectId;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B name(String name) {
			ensureOrganisation();
			organization.put(NAME, name);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B kvk(String kvk) {
			ensureOrganisation();
			organization.put(KVK, kvk);
			return (B) this;
		}

		private void ensureClaims() {
			if (claims == null) {
				claims = new LinkedHashMap<>();
			}
		}

		private void ensureOrganisation() {
			ensureClaims();
			if (organization == null) {
				organization = new HashMap<>();
				claims.put(ORGANIZATION, organization);
			}
		}

	}

	public HealthcareproviderDetailsExcerptCredential(Map<String, Object> jsonObject) {
		super(jsonObject);
	}

	public HealthcareproviderDetailsExcerptCredential() {
		super();
	}

	public static Builder<? extends Builder<?>> builder() {
		return new Builder<>(new HealthcareproviderDetailsExcerptCredential());
	}

	public static HealthcareproviderDetailsExcerptCredential fromJsonObject(Map<String, Object> jsonObject) {
		HealthcareproviderDetailsExcerptCredential result = new HealthcareproviderDetailsExcerptCredential(jsonObject);
		if (!CollectionUtils.emptyIfNull(result.getTypes()).contains(TYPE)) {
			return null;
		}
		return result;
	}

	public static HealthcareproviderDetailsExcerptCredential fromJsonLDObject(JsonLDObject jsonLDObject) {
		return fromJsonObject(jsonLDObject.getJsonObject());
	}

	public static HealthcareproviderDetailsExcerptCredential fromJson(Reader reader) {
		return fromJsonObject(readJson(reader));
	}

	public static HealthcareproviderDetailsExcerptCredential fromJson(String json) {
		return fromJsonObject(readJson(json));
	}

	public static HealthcareproviderDetailsExcerptCredential fromMap(Map<String, Object> map) {
		return fromJsonObject(map);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getOrganisation() {
		return Optional.ofNullable(getCredentialSubject()) //
				.map(CredentialSubject::getClaims) // //
				.map(claims -> claims.get(ORGANIZATION)) //
				.stream() //
				.filter(Map.class::isInstance) //
				.map(Map.class::cast) //
				.findFirst() //
				.orElse(Map.of());
	}

	private <T> T getFromOrganisation(String claim, Class<T> clazz) {
		return Optional.ofNullable(getOrganisation()) //
				.map(o -> o.get(claim)) //
				.filter(clazz::isInstance) //
				.map(clazz::cast) //
				.orElse(null);
	}

	public String getName() {
		return getFromOrganisation(NAME, String.class);
	}

	public String getKVK() {
		return getFromOrganisation(KVK, String.class);
	}

}
