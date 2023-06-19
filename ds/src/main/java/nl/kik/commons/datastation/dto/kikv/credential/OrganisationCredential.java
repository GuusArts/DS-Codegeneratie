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
 * @author michael
 *
 */
public class OrganisationCredential extends VerifiableCredential {
	public static final String TYPE = "OrganisationCredential";
	public static final URI URL = URI.create("https://kik-v.nl/organisation/v1.json");

	public static final String VALIDATED_QUERY = "kvk";
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

			CONTEXTS.put(URL, JsonDocument.of(MediaType.JSON_LD,
					OrganisationCredential.class.getResourceAsStream("OrganisationCredential.ldjson")));

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

		public Builder(OrganisationCredential jsonLdObject) {
			super(jsonLdObject);
		}

		@Override
		public OrganisationCredential build() {
			super.build();

			if (MapUtils.isNotEmpty(claims) || subjectId != null) {
				CredentialSubject credentialSubject = CredentialSubject.builder() //
						.id(subjectId) //
						.claims(claims) //
						.build();
				credentialSubject.addToJsonLDObject(this.jsonLdObject);
			}

			return (OrganisationCredential) this.jsonLdObject;
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
			ensureClaims();
			claims.put(NAME, name);
			return (B) this;
		}

		private void ensureClaims() {
			if (claims == null) {
				claims = new LinkedHashMap<>();
			}
		}
	}

	public OrganisationCredential(Map<String, Object> jsonObject) {
		super(jsonObject);
	}

	public OrganisationCredential() {
		super();
	}

	public static Builder<? extends Builder<?>> builder() {
		return new Builder<>(new OrganisationCredential());
	}

	public static OrganisationCredential fromJsonObject(Map<String, Object> jsonObject) {
		OrganisationCredential result = new OrganisationCredential(jsonObject);
		if (!CollectionUtils.emptyIfNull(result.getTypes()).contains(TYPE)) {
			return null;
		}
		return result;
	}

	public static OrganisationCredential fromJsonLDObject(JsonLDObject jsonLDObject) {
		return fromJsonObject(jsonLDObject.getJsonObject());
	}

	public static OrganisationCredential fromJson(Reader reader) {
		return fromJsonObject(readJson(reader));
	}

	public static OrganisationCredential fromJson(String json) {
		return fromJsonObject(readJson(json));
	}

	public static OrganisationCredential fromMap(Map<String, Object> map) {
		return fromJsonObject(map);
	}

	private <T> Optional<T> getFromSubject(String claim, Class<T> clazz) {
		return Optional.ofNullable(getCredentialSubject()) //
				.map(CredentialSubject::getClaims) // //
				.map(claims -> claims.get(claim)) //
				.filter(clazz::isInstance) //
				.map(clazz::cast) //
		;
	}

	public URI getSubjectId() {
		return Optional.of(getCredentialSubject()) //
				.map(subject -> subject.getId()) //
				.orElse(null);
	}

	public String getName() {
		return getFromSubject(NAME, String.class).orElse(null);
	}
}
