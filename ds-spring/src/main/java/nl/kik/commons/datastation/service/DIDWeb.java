package nl.kik.commons.datastation.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import foundation.identity.did.DIDDocument;
import lombok.extern.slf4j.Slf4j;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

@Slf4j
public class DIDWeb implements Driver {
	public static final String MIME_TYPES = DIDDocument.MIME_TYPE_JSON_LD + "," + "application/ld+json";

	private final HttpClient httpClient = HttpClients.createDefault();
	private final Pattern pattern = Pattern.compile("^(did:web:)([^:]+)(:.+)?$");
	private boolean allowLocalhost = false;
	private int localhostPort = 8280;

	public DIDWeb() {

	}

	public int getLocalhostPort() {
		return localhostPort;
	}

	public boolean isAllowLocalhost() {
		return allowLocalhost;
	}

	@Override
	public Map<String, Object> properties() throws ResolutionException {
		final Map<String, Object> httpProperties = new HashMap<>();
		httpProperties.put("pattern", pattern.toString());
		final Map<String, Object> properties = new HashMap<>();
		properties.put("http", httpProperties);
		return properties;
	}

	@Override
	public ResolveResult resolve(final String identifier) throws ResolutionException {
		// match identifier
		final Matcher matcher = pattern.matcher(identifier);
		if (!matcher.matches()) {
			DIDWeb.log.debug("Skipping identifier {} - does not match pattern {}", identifier, pattern);
			return null;
		}
		DIDWeb.log.debug("Identifier {} matches pattern {} with {} groups", identifier, pattern, matcher.groupCount());

		String url;
		if ("localhost".equals(matcher.group(2))) {
			if (!allowLocalhost) {
				return null;
			}
			url = "http://localhost:" + getLocalhostPort();
		} else {
			url = "https://" + matcher.group(2);
		}
		if (StringUtils.isNotBlank(matcher.group(3))) {
			url = url + matcher.group(3).replace(':', '/');
		} else {
			url = url + "/.well-known";
		}
		url = url + "/did.json";

		final HttpGet httpGet = new HttpGet(URI.create(url));
		httpGet.addHeader("Accept", DIDWeb.MIME_TYPES);

		// execute HTTP request

		ResolveResult resolveResult;

		DIDWeb.log.debug("Request for identifier {} to: ", identifier, url);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(httpGet)) {
			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			final String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			DIDWeb.log.debug("Response status from {}: {} {}", url, statusCode, statusMessage);
			if (statusCode == 404) {
				return null;
			}

			final HttpEntity httpEntity = httpResponse.getEntity();
			final String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			DIDWeb.log.debug("Response body from {}: {}", url, httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {
				DIDWeb.log.warn("Cannot retrieve RESOLVE RESULT for {} from {}: {}", identifier, url, httpBody);
				throw new ResolutionException(httpBody);
			}

			resolveResult = ResolveResult.build(DIDDocument.fromJson(httpBody));
			if (resolveResult.getDidDocument() == null
					|| !identifier.equals(resolveResult.getDidDocument().getId().toString())) {
				throw new ResolutionException("Result does not match requested identifier " + identifier + " != "
						+ resolveResult.getDidDocument().getId());
			}
		} catch (final IOException ex) {
			throw new ResolutionException(
					"Cannot retrieve RESOLVE RESULT for " + identifier + " from " + url + ": " + ex.getMessage(), ex);
		}

		DIDWeb.log.debug("Retrieved RESOLVE RESULT for {} ({}): {}", identifier, url, resolveResult);

		return resolveResult;
	}

	public void setAllowLocalhost(final boolean allowLocalhost) {
		this.allowLocalhost = allowLocalhost;
	}

	public void setLocalhostPort(final int localhostPort) {
		this.localhostPort = localhostPort;
	}

}
