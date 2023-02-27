package nl.kik.commons.datastation.dto.nuts;

import java.io.Reader;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.danubetech.verifiablecredentials.VerifiableCredential;

import foundation.identity.jsonld.JsonLDObject;
import foundation.identity.jsonld.JsonLDUtils;

public class CreateVerifiableCredential extends VerifiableCredential {
    public static String VISIBILITY = "visibility";
    public static final String PUBLISH_TO_NETWORK = "publishToNetwork";

    public static class Builder<B extends Builder<B>> extends VerifiableCredential.Builder<B> {
        private Visibility visibility = Visibility.Private;
        private boolean publishToNetwork = true;

        public Builder(VerifiableCredential jsonLdObject) {
            super(fromJsonLDObject(jsonLdObject));
        }

        @Override
        public CreateVerifiableCredential build() {
            super.build();
            if (this.visibility != null)
                JsonLDUtils.jsonLdAdd(this.jsonLdObject, VISIBILITY, visibility.toString());
            JsonLDUtils.jsonLdAdd(this.jsonLdObject, PUBLISH_TO_NETWORK, publishToNetwork);
            return (CreateVerifiableCredential) this.jsonLdObject;
        }

        @SuppressWarnings("unchecked")
        public B visibility(Visibility visibility) {
            this.visibility = visibility;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B publishToNetwork(boolean publishToNetwork) {
            this.publishToNetwork = publishToNetwork;
            return (B) this;
        }

    }

    public static Builder<? extends Builder<?>> builder(VerifiableCredential base) {
        return new Builder<>(base);
    }

    public CreateVerifiableCredential(Map<String, Object> jsonObject) {
        super(jsonObject);
    }

    public CreateVerifiableCredential() {
        super();
    }

    public static CreateVerifiableCredential fromJsonObject(Map<String, Object> jsonObject) {
        return new CreateVerifiableCredential(jsonObject);
    }

    public static CreateVerifiableCredential fromJsonLDObject(JsonLDObject jsonLDObject) {
        return fromJsonObject(jsonLDObject.getJsonObject());
    }

    public static CreateVerifiableCredential fromJson(Reader reader) {
        return new CreateVerifiableCredential(readJson(reader));
    }

    public static CreateVerifiableCredential fromJson(String json) {
        return new CreateVerifiableCredential(readJson(json));
    }

    public static CreateVerifiableCredential fromMap(Map<String, Object> map) {
        return new CreateVerifiableCredential(map);
    }

    public Visibility getVisibility() {
        return switch (StringUtils.lowerCase(JsonLDUtils.jsonLdGetString(this.getJsonObject(), VISIBILITY))) {
        case "private" -> Visibility.Private;
        case "public" -> Visibility.Public;
        default -> null;
        };
    }

    public boolean getPublishToNetwork() {
        return switch (Objects.requireNonNullElse(this.getJsonObject().get(PUBLISH_TO_NETWORK), "false").toString()
                .toLowerCase()) {
        case "true" -> true;
        default -> false;
        };
    }
}
