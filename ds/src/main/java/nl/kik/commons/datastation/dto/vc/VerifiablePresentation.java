package nl.kik.commons.datastation.dto.vc;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialContexts;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialKeywords;
import com.fasterxml.jackson.annotation.JsonCreator;
import foundation.identity.jsonld.JsonLDObject;
import foundation.identity.jsonld.JsonLDUtils;
import info.weboftrust.ldsignatures.LdProof;

import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Basiucally a copy of
 * com.danubetech.verifiablecredentials.VerifiablePresentation allowing multiple
 * VCs
 * 
 * @author michael
 *
 */
public class VerifiablePresentation extends JsonLDObject {

	public static final URI[] DEFAULT_JSONLD_CONTEXTS = {
			VerifiableCredentialContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_V1 };
	public static final String[] DEFAULT_JSONLD_TYPES = {
			VerifiableCredentialKeywords.JSONLD_TERM_VERIFIABLE_PRESENTATION };
	public static final String DEFAULT_JSONLD_PREDICATE = null;
	public static final DocumentLoader DEFAULT_DOCUMENT_LOADER = VerifiableCredentialContexts.DOCUMENT_LOADER;

	@JsonCreator
	public VerifiablePresentation() {
		super();
	}

	protected VerifiablePresentation(Map<String, Object> jsonObject) {
		super(jsonObject);
	}

	/*
	 * Factory methods
	 */

	public static class Builder<B extends Builder<B>> extends JsonLDObject.Builder<B> {

		private URI holder;
		List<VerifiableCredential> vcs;
		private LdProof ldProof;

		public Builder(VerifiablePresentation jsonLdObject) {
			super(jsonLdObject);
			this.forceContextsArray(true);
			this.forceTypesArray(true);
			this.defaultContexts(true);
			this.defaultTypes(true);
		}

		@Override
		public VerifiablePresentation build() {
			super.build();
			// add JSON-LD properties
			if (this.holder != null)
				JsonLDUtils.jsonLdAdd(this.jsonLdObject, VerifiableCredentialKeywords.JSONLD_TERM_HOLDER,
						JsonLDUtils.uriToString(this.holder));
			if (this.vcs != null) {
				if (vcs.size() > 1) {
					JsonLDUtils.jsonLdAddAsJsonArray(this.jsonLdObject, VerifiableCredential.DEFAULT_JSONLD_PREDICATE,
							vcs);
				} else {
					this.vcs.iterator().next().addToJsonLDObject(this.jsonLdObject);
				}
			}
			if (this.ldProof != null)
				this.ldProof.addToJsonLDObject(this.jsonLdObject);

			return (VerifiablePresentation) this.jsonLdObject;
		}

		@SuppressWarnings("unchecked")
		public B holder(URI holder) {
			this.holder = holder;
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B verifiableCredential(VerifiableCredential verifiableCredential) {
			ensureVcs();
			vcs.add(verifiableCredential);
			return (B) this;
		}

		@SuppressWarnings("unchecked")
		public B verifiableCredentials(Collection<VerifiableCredential> verifiableCredential) {
			ensureVcs();
			vcs.addAll(verifiableCredential);
			return (B) this;
		}

		private void ensureVcs() {
			if (this.vcs == null) {
				this.vcs = new ArrayList<>();
			}
		}

		@SuppressWarnings("unchecked")
		public B ldProof(LdProof ldProof) {
			this.ldProof = ldProof;
			return (B) this;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Builder<? extends Builder<?>> builder() {
		return new Builder(new VerifiablePresentation());
	}

	public static VerifiablePresentation fromJsonObject(Map<String, Object> jsonObject) {
		return new VerifiablePresentation(jsonObject);
	}

	public static VerifiablePresentation fromJsonLDObject(JsonLDObject jsonLDObject) {
		return fromJsonObject(jsonLDObject.getJsonObject());
	}

	public static VerifiablePresentation fromJson(Reader reader) {
		return new VerifiablePresentation(readJson(reader));
	}

	public static VerifiablePresentation fromJson(String json) {
		return new VerifiablePresentation(readJson(json));
	}

	public static VerifiablePresentation fromMap(Map<String, Object> jsonObject) {
		return new VerifiablePresentation(jsonObject);
	}

	/*
	 * Adding, getting, and removing the JSON-LD object
	 */

	public static VerifiablePresentation getFromJsonLDObject(JsonLDObject jsonLdObject) {
		return JsonLDObject.getFromJsonLDObject(VerifiablePresentation.class, jsonLdObject);
	}

	public static void removeFromJsonLdObject(JsonLDObject jsonLdObject) {
		JsonLDObject.removeFromJsonLdObject(VerifiablePresentation.class, jsonLdObject);
	}

	/*
	 * Getters
	 */

	public URI getHolder() {
		return JsonLDUtils.stringToUri(JsonLDUtils.jsonLdGetStringOrObjectId(this.getJsonObject(),
				VerifiableCredentialKeywords.JSONLD_TERM_HOLDER));
	}

	public Collection<VerifiableCredential> getVerifiableCredentials() {
		Object credential = JsonLDUtils.jsonLdGetJsonValue(getJsonObject(),
				VerifiableCredential.DEFAULT_JSONLD_PREDICATE);
		if (credential instanceof List<?> l) {
			return l.stream() //
					.map(this::getCredential) //
					.toList();
		} else if (credential != null) {
			return List.of(getCredential(credential));
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	private VerifiableCredential getCredential(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Map<?, ?> map) {
			return VerifiableCredential.fromMap((Map<String, Object>) map);
		}
		throw new IllegalArgumentException("Received unexpected JSON-LD object of type " + o.getClass());
	}

	public LdProof getLdProof() {
		return LdProof.getFromJsonLDObject(this);
	}
}
