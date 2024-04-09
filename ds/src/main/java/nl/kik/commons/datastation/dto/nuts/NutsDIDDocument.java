package nl.kik.commons.datastation.dto.nuts;

import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.danubetech.verifiablecredentials.CredentialSubject;

import foundation.identity.did.DIDDocument;
import foundation.identity.did.jsonld.DIDContexts;
import foundation.identity.jsonld.ConfigurableDocumentLoader;
import foundation.identity.jsonld.JsonLDObject;
import foundation.identity.jsonld.JsonLDUtils;

public class NutsDIDDocument extends DIDDocument {
    public static final URI URL = URI.create("https://nuts.nl/did/v1");

    public static final String CONTROLLER = "controller";

    public static final URI[] DEFAULT_JSONLD_CONTEXTS = { DIDContexts.JSONLD_CONTEXT_W3_NS_DID_V1, URL };
    public static final String[] DEFAULT_JSONLD_TYPES = {};
    public static final String DEFAULT_JSONLD_PREDICATE = null;
    public static final DocumentLoader DEFAULT_DOCUMENT_LOADER;
    public static final Map<URI, JsonDocument> CONTEXTS;

    static {
        try {
            CONTEXTS = new HashMap<>();
            CONTEXTS.putAll(DIDContexts.CONTEXTS);

            CONTEXTS.put(URL, JsonDocument.of(MediaType.JSON_LD,
                    NutsDIDDocument.class.getResourceAsStream("NutsDIDDocument.ldjson")));

            for (Map.Entry<URI, JsonDocument> context : CONTEXTS.entrySet()) {
                context.getValue().setDocumentUrl(context.getKey());
            }
        } catch (JsonLdError ex) {
            throw new ExceptionInInitializerError(ex);
        }

        DEFAULT_DOCUMENT_LOADER = new ConfigurableDocumentLoader(CONTEXTS);
    }

    public static class Builder<B extends Builder<B>> extends DIDDocument.Builder<B> {
        private Map<String, Object> claims;
        private List<URI> controllers;

        public Builder(NutsDIDDocument jsonLdObject) {
            super(jsonLdObject);
        }

        @Override
        public NutsDIDDocument build() {
            super.build();

            if (MapUtils.isNotEmpty(claims)) {
                CredentialSubject credentialSubject = CredentialSubject.builder() //
                        .claims(claims) //
                        .build();
                credentialSubject.addToJsonLDObject(this.jsonLdObject);
            }
            if (CollectionUtils.isNotEmpty(controllers)) {
                JsonLDUtils.jsonLdAddAsJsonArray(this.jsonLdObject, CONTROLLER, claims);
            }

            return (NutsDIDDocument) this.jsonLdObject;
        }

        @SuppressWarnings("unchecked")
        public B claims(Map<String, Object> claims) {
            this.claims = claims;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B controllers(List<URI> controllers) {
            ensureControllers();
            this.controllers.addAll(controllers);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B controller(URI controller) {
            ensureControllers();
            this.controllers.add(controller);
            return (B) this;
        }

        private void ensureControllers() {
            if (controllers == null) {
                controllers = new ArrayList<>();
            }
        }
    }

    public NutsDIDDocument(Map<String, Object> jsonObject) {
        super(jsonObject);
    }

    public NutsDIDDocument() {
        super();
    }

    public static Builder<? extends Builder<?>> builder() {
        return new Builder<>(new NutsDIDDocument());
    }

    public static NutsDIDDocument fromJsonObject(Map<String, Object> jsonObject) {
        return new NutsDIDDocument(jsonObject);
    }

    public static NutsDIDDocument fromJsonLDObject(JsonLDObject jsonLDObject) {
        return fromJsonObject(jsonLDObject.getJsonObject());
    }

    public static NutsDIDDocument fromJson(Reader reader) {
        return new NutsDIDDocument(readJson(reader));
    }

    public static NutsDIDDocument fromJson(String json) {
        return new NutsDIDDocument(readJson(json));
    }

    public static NutsDIDDocument fromMap(Map<String, Object> map) {
        return new NutsDIDDocument(map);
    }

    public List<URI> getControllers() {
        return JsonLDUtils.jsonLdGetStringList(this.getJsonObject(), CONTROLLER)
                .stream().map(URI::create).toList();
    }

}
