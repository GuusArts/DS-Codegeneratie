package nl.kik.datastation.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.JOSEException;

import lombok.extern.slf4j.Slf4j;
import nl.kik.datastation.dto.ds.async.Request;
import nl.kik.datastation.dto.ds.async.ReturnMessage;
import nl.kik.datastation.dto.vc.VerifiablePresentation;
import nl.kik.datastation.mvc.RequestMessageConverter;
import nl.kik.datastation.mvc.ResponseMessageConverter;

@Slf4j
public class RemoteDatastationService {
	private final RestTemplate rest;
	private final ResponseMessageConverter responseConverter;
	private final RequestMessageConverter requestConverter;

	public RemoteDatastationService(final RestTemplate rest, final ResponseMessageConverter responseConverter,
			final RequestMessageConverter requestConverter) {
		this.rest = rest;
		this.responseConverter = responseConverter;
		this.requestConverter = requestConverter;
	}

	public void request(final String url, final Request<VerifiablePresentation> request)
			throws RestClientException, JOSEException, Exception {
		RemoteDatastationService.log.trace("Sending request {} to {}", request, url);
		final ResponseEntity<Void> result = rest.postForEntity(url, requestConverter.encode(request), Void.class);
		RemoteDatastationService.log.trace("Received for request {}", result.getStatusCode());
		if (result.getStatusCode().isError())
			throw new IllegalArgumentException(result.getStatusCode().getReasonPhrase());
	}

	@SuppressWarnings("unchecked")
	public void response(final String url, final ReturnMessage<? extends Object> response)
			throws RestClientException, JOSEException, Exception {
		RemoteDatastationService.log.trace("Sending response {} to {}", response, url);
		final ResponseEntity<Void> result = rest.postForEntity(url,
				responseConverter.encode((ReturnMessage<Object>) response), Void.class);
		RemoteDatastationService.log.trace("Received for response {}", result.getStatusCode());
		if (result.getStatusCode().isError())
			throw new IllegalArgumentException(result.getStatusCode().getReasonPhrase());
	}
}
