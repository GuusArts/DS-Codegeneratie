package nl.kik.commons.datastation.dto.kikv;

import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

public class ValidatedQueryCredential extends VerifiableCredential {
	public static final String TYPE = "ValidatedQueryCredential";
	public static final URI URL = URI.create("https://kik-v-nuts.ams3.cdn.digitaloceanspaces.com");

	public static final String NAME = "name";
	public static final String PARAMS_SHACL = "paramsSHACL";
	public static final String SPARQL = "sparql";
	public static final String ONTOLOGY = "ontology";
	public static final String PROFILE = "profile";
	public static final String DESCRIPTION = "description";

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
					ValidatedQueryCredential.class.getResourceAsStream("ValidatedQueryCredential.ldjson")));

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
		private URI queryId;

		public Builder(ValidatedQueryCredential jsonLdObject) {
			super(jsonLdObject);
		}

		@Override
		public ValidatedQueryCredential build() {
			super.build();

			if (MapUtils.isNotEmpty(claims)) {
				CredentialSubject credentialSubject = CredentialSubject.builder() //
						.id(queryId) //
						.claims(claims) //
						.build();
				credentialSubject.addToJsonLDObject(this.jsonLdObject);
			}

			return (ValidatedQueryCredential) this.jsonLdObject;
		}

		@SuppressWarnings("unchecked")
		public B claims(Map<String, Object> claims) {
			this.claims = claims;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B queryId(URI queryId) {
			this.queryId = queryId;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B name(String name) {
			ensureClaims();
			claims.put(NAME, name);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B description(String description) {
			ensureClaims();
			claims.put(DESCRIPTION, description);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B profile(String profile) {
			ensureClaims();
			claims.put(PROFILE, profile);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B ontology(String ontology) {
			ensureClaims();
			claims.put(ONTOLOGY, ontology);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B sparql(String sparql) {
			ensureClaims();
			claims.put(SPARQL, sparql);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B paramsSHACL(String paramsSHACL) {
			ensureClaims();
			claims.put(PARAMS_SHACL, paramsSHACL);
			return (B) this;
		}

		private void ensureClaims() {
			if (claims == null) {
				claims = new LinkedHashMap<>();
			}
		}
	}

	public ValidatedQueryCredential(Map<String, Object> jsonObject) {
		super(jsonObject);
	}

	public ValidatedQueryCredential() {
		super();
	}

	public static Builder<? extends Builder<?>> builder() {
		return new Builder<>(new ValidatedQueryCredential());
	}

	public static ValidatedQueryCredential fromJsonObject(Map<String, Object> jsonObject) {
		return new ValidatedQueryCredential(jsonObject);
	}

	public static ValidatedQueryCredential fromJsonLDObject(JsonLDObject jsonLDObject) {
		return fromJsonObject(jsonLDObject.getJsonObject());
	}

	public static ValidatedQueryCredential fromJson(Reader reader) {
		return new ValidatedQueryCredential(readJson(reader));
	}

	public static ValidatedQueryCredential fromJson(String json) {
		return new ValidatedQueryCredential(readJson(json));
	}

	public static ValidatedQueryCredential fromMap(Map<String, Object> map) {
		return new ValidatedQueryCredential(map);
	}

	private <T> T getFromSubject(Function<CredentialSubject, T> extractor) {
		return Optional.ofNullable(getCredentialSubject()) //
				.map(extractor) //
				.orElse(null);
	}

	private <T> T getFromSubject(String claim, Class<T> clazz) {
		return Optional.ofNullable(getCredentialSubject()) //
				.map(CredentialSubject::getClaims) // //
				.map(claims -> claims.get(claim)) //
				.filter(clazz::isInstance) //
				.map(clazz::cast) //
				.orElse(null);
	}

	public URI getQueryId() {
		return getFromSubject(s -> s.getId());
	}

	public String getName() {
		return getFromSubject(NAME, String.class);
	}

	public String getDescription() {
		return getFromSubject(DESCRIPTION, String.class);
	}

	public String getProfile() {
		return getFromSubject(PROFILE, String.class);
	}

	public String getOntology() {
		return getFromSubject(ONTOLOGY, String.class);
	}

	public String getSPARQL() {
		return getFromSubject(SPARQL, String.class);
	}

	public String getParamsSHACL() {
		return getFromSubject(PARAMS_SHACL, String.class);
	}

}
