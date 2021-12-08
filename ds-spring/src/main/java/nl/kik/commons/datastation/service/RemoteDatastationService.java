package nl.kik.commons.datastation.service;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import nl.kik.commons.datastation.dto.ds.async.Request;
import nl.kik.commons.datastation.dto.ds.async.ReturnMessage;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;
import nl.kik.commons.datastation.mvc.RequestMessageConverter;
import nl.kik.commons.datastation.mvc.ResponseMessageConverter;

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

	public void request(final String url, final Request<VerifiablePresentation> request) throws Exception {
		RemoteDatastationService.log.trace("Sending request {} to {}", request, url);
		final ResponseEntity<Void> result = rest.postForEntity(url, requestConverter.encode(request), Void.class);
		RemoteDatastationService.log.trace("Received for request {}", result.getStatusCode());
		if (result.getStatusCode().isError()) {
			throw new IllegalArgumentException(result.getStatusCode().getReasonPhrase());
		}
	}

	@SuppressWarnings("unchecked")
	public void response(final String url, final ReturnMessage<? extends Object> response) throws Exception {
		RemoteDatastationService.log.trace("Sending response {} to {}", response, url);
		final ResponseEntity<Void> result = rest.postForEntity(url,
				responseConverter.encode((ReturnMessage<Object>) response), Void.class);
		RemoteDatastationService.log.trace("Received for response {}", result.getStatusCode());
		if (result.getStatusCode().isError()) {
			throw new IllegalArgumentException(result.getStatusCode().getReasonPhrase());
		}
	}

	public ValidationReport validate(final String url, final Shapes shapes) {
		RemoteDatastationService.log.trace("Sending shapes {} to {}", shapes, url);
		final RequestCallback request = r -> {
			r.getHeaders().put(HttpHeaders.CONTENT_TYPE, List.of(Lang.TURTLE.getContentType().toHeaderString()));
			r.getHeaders().put(HttpHeaders.ACCEPT, List.of(Lang.TURTLE.getContentType().toHeaderString()));
			final Model model = new ShaclExporter(ModelFactory.createDefaultModel()).export(shapes);
			RDFDataMgr.write(r.getBody(), model, RDFFormat.TURTLE_FLAT);
		};
		final ResponseExtractor<ValidationReport> response = r -> {
			final Model model = ModelFactory.createDefaultModel();
			RDFDataMgr.read(model, r.getBody(), Lang.TURTLE);
			return ValidationReport.fromModel(model);
		};
		return rest.execute(url, HttpMethod.POST, request, response);
	}
}
