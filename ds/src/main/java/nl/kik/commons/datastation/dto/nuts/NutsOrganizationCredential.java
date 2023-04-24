package nl.kik.commons.datastation.dto.nuts;

import java.io.Reader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NutsOrganizationCredential extends VerifiableCredential {
	public static final String TYPE = "NutsOrganizationCredential";
	public static final URI URL = URI.create("https://nuts.nl/credentials/v1");

	public static final String ORGANIZATION = "organization";
	public static final String NAME = "name";
	public static final String CITY = "city";

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
					NutsOrganizationCredential.class.getResourceAsStream("NutsOrganizationCredential.ldjson")));

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
		private Map<String, Object> organization;
		private URI orgId;

		public Builder(NutsOrganizationCredential jsonLdObject) {
			super(jsonLdObject);
		}

		@Override
		public NutsOrganizationCredential build() {
			super.build();

			if (MapUtils.isNotEmpty(claims) || orgId != null) {
				CredentialSubject credentialSubject = CredentialSubject.builder() //
						.id(orgId) //
						.claims(claims) //
						.build();
				credentialSubject.addToJsonLDObject(this.jsonLdObject);
			}

			return (NutsOrganizationCredential) this.jsonLdObject;
		}

		@SuppressWarnings("unchecked")
		public B claims(Map<String, Object> claims) {
			this.claims = claims;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B orgId(URI orgId) {
			this.orgId = orgId;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B name(String name) {
			ensureOrganisation();
			organization.put(NAME, name);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B city(String city) {
			ensureOrganisation();
			organization.put(CITY, city);
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

	public NutsOrganizationCredential(Map<String, Object> jsonObject) {
		super(jsonObject);
	}

	public NutsOrganizationCredential() {
		super();
	}

	public static Builder<? extends Builder<?>> builder() {
		return new Builder<>(new NutsOrganizationCredential());
	}

	public static NutsOrganizationCredential fromJsonObject(Map<String, Object> jsonObject) {
		return new NutsOrganizationCredential(jsonObject);
	}

	public static NutsOrganizationCredential fromJsonLDObject(JsonLDObject jsonLDObject) {
		return fromJsonObject(jsonLDObject.getJsonObject());
	}

	public static NutsOrganizationCredential fromJson(Reader reader) {
		return new NutsOrganizationCredential(readJson(reader));
	}

	public static NutsOrganizationCredential fromJson(String json) {
		return new NutsOrganizationCredential(readJson(json));
	}

	public static NutsOrganizationCredential fromMap(Map<String, Object> map) {
		return new NutsOrganizationCredential(map);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getOrganisation() {
		return Optional.ofNullable(getCredentialSubject()) //
				.map(CredentialSubject::getClaims) // //
				.map(claims -> claims.get(ORGANIZATION)) //
				.stream() //
				.peek(c -> log.info("Claims {}: {}", c, c.getClass())) //
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

	public String getCity() {
		return getFromOrganisation(CITY, String.class);
	}
}
