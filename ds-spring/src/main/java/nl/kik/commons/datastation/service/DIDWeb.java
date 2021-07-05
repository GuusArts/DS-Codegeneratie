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

	private HttpClient httpClient = HttpClients.createDefault();
	private Pattern pattern = Pattern.compile("^(did:web:)([^:]+)(:.+)?$");
	private boolean allowLocalhost = false;
	private int localhostPort = 8280;

	public DIDWeb() {

	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
		// match identifier
		Matcher matcher = pattern.matcher(identifier);
		if (!matcher.matches()) {
			log.debug("Skipping identifier {} - does not match pattern {}", identifier, pattern);
			return null;
		} else {
			log.debug("Identifier {} matches pattern {} with {} groups", identifier, pattern, matcher.groupCount());
		}

		String url;
		if ("localhost".equals(matcher.group(2))) {
			if (allowLocalhost) {
				url = "http://localhost:" + getLocalhostPort();
			} else {
				return null;
			}
		} else {
			url = "https://" + matcher.group(2);
		}
		if (StringUtils.isNotBlank(matcher.group(3))) {
			url = url + matcher.group(3).replace(':', '/');
		} else {
			url = url + "/.well-known";
		}
		url = url + "/did.json";

		HttpGet httpGet = new HttpGet(URI.create(url));
		httpGet.addHeader("Accept", MIME_TYPES);

		// execute HTTP request

		ResolveResult resolveResult;

		log.debug("Request for identifier {} to: ", identifier, url);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(httpGet)) {
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			log.debug("Response status from {}: {} {}", url, statusCode, statusMessage);
			if (statusCode == 404)
				return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			log.debug("Response body from {}: {}", url, httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {
				log.warn("Cannot retrieve RESOLVE RESULT for {} from {}: {}", identifier, url, httpBody);
				throw new ResolutionException(httpBody);
			}

			resolveResult = ResolveResult.build(DIDDocument.fromJson(httpBody));
			if (resolveResult.getDidDocument() == null
					|| !identifier.equals(resolveResult.getDidDocument().getId().toString())) {
				throw new ResolutionException("Result does not match requested identifier " + identifier + " != "
						+ resolveResult.getDidDocument().getId());
			}
		} catch (IOException ex) {
			throw new ResolutionException(
					"Cannot retrieve RESOLVE RESULT for " + identifier + " from " + url + ": " + ex.getMessage(), ex);
		}

		log.debug("Retrieved RESOLVE RESULT for {} ({}): {}", identifier, url, resolveResult);

		return resolveResult;
	}

	@Override
	public Map<String, Object> properties() throws ResolutionException {
		Map<String, Object> httpProperties = new HashMap<String, Object>();
		httpProperties.put("pattern", pattern.toString());
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("http", httpProperties);
		return properties;
	}

	public boolean isAllowLocalhost() {
		return allowLocalhost;
	}

	public void setAllowLocalhost(boolean allowLocalhost) {
		this.allowLocalhost = allowLocalhost;
	}

	public int getLocalhostPort() {
		return localhostPort;
	}

	public void setLocalhostPort(int localhostPort) {
		this.localhostPort = localhostPort;
	}

}
