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
	private RestTemplate rest;
	private ResponseMessageConverter responseConverter;
	private RequestMessageConverter requestConverter;

	public RemoteDatastationService(RestTemplate rest, ResponseMessageConverter responseConverter,
			RequestMessageConverter requestConverter) {
		this.rest = rest;
		this.responseConverter = responseConverter;
		this.requestConverter = requestConverter;
	}

	public void request(String url, Request<VerifiablePresentation> request)
			throws RestClientException, JOSEException, Exception {
		log.trace("Sending request {} to {}", request, url);
		ResponseEntity<Void> result = rest.postForEntity(url, requestConverter.encode(request), Void.class);
		log.trace("Received for request {}", result.getStatusCode());
		if (result.getStatusCode().isError()) {
			throw new IllegalArgumentException(result.getStatusCode().getReasonPhrase());
		}
	}

	@SuppressWarnings("unchecked")
	public void response(String url, ReturnMessage<? extends Object> response)
			throws RestClientException, JOSEException, Exception {
		log.trace("Sending response {} to {}", response, url);
		ResponseEntity<Void> result = rest.postForEntity(url,
				responseConverter.encode((ReturnMessage<Object>) response), Void.class);
		log.trace("Received for response {}", result.getStatusCode());
		if (result.getStatusCode().isError()) {
			throw new IllegalArgumentException(result.getStatusCode().getReasonPhrase());
		}
	}
}
