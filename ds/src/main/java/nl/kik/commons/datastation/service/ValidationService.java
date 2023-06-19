package nl.kik.commons.datastation.service;

import java.net.URI;
import java.text.ParseException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;

import nl.kik.commons.datastation.dto.didcomm.Code;
import nl.kik.commons.datastation.dto.didcomm.Message;
import nl.kik.commons.datastation.dto.didcomm.ProblemReport;
import nl.kik.commons.datastation.dto.didcomm.Scope;
import nl.kik.commons.datastation.dto.didcomm.Sorter;
import nl.kik.commons.datastation.dto.kikv.QueryRequest;
import nl.kik.commons.datastation.dto.kikv.QueryResponse;
import nl.kik.commons.datastation.dto.kikv.Request;
import nl.kik.commons.datastation.dto.kikv.Response;
import nl.kik.commons.datastation.dto.kikv.Result;
import nl.kik.commons.datastation.dto.kikv.ResultSet;
import nl.kik.commons.datastation.dto.kikv.credential.ValidatedQueryCredential;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;

public class ValidationService {
	private CryptoService crypto;
	private boolean ignoreVP;

	public ValidationService(CryptoService crypto, boolean ignoreVP) {
		this.crypto = crypto;
		this.ignoreVP = ignoreVP;
	}

	public void validate(JWT oauth, String service) throws ParseException {
		JWTClaimsSet claims = oauth.getJWTClaimsSet();
		if (StringUtils.isBlank(claims.getIssuer())) {
			throw new IllegalArgumentException("Missing issuer");
		}
		if (StringUtils.isBlank(claims.getSubject())) {
			throw new IllegalArgumentException("Missing subject");
		}
		if (!StringUtils.equals(service, claims.getStringClaim("service"))) {
			throw new IllegalArgumentException("Tokens contains unexpected service");
		}
	}

	public void validate(Message<?> m) {
		if (m.getId() == null) {
			throw new IllegalArgumentException("Message has no id");
		}
		if (m.getFrom() == null) {
			throw new IllegalArgumentException("Message is missing from");
		}
		if (CollectionUtils.isEmpty(m.getTo())) {
			throw new IllegalArgumentException("No recipient in message");
		}
	}

	public void validate(Message<?> m, JWT oauth) throws ParseException {
		JWTClaimsSet claims = oauth.getJWTClaimsSet();
		if (m.getFrom() == null || !StringUtils.equals(claims.getSubject(), m.getFrom().toString())) {
			throw new IllegalArgumentException("OAuth subject does not match message from");
		}
		if (!CollectionUtils.emptyIfNull(m.getTo()).contains(URI.create(claims.getIssuer()))) {
			throw new IllegalArgumentException("OAuth issuer does not match message to");
		}
	}

	public void validate(Request request, Response response) {
		if (!Objects.equals(request.getId(), response.getThid())) {
			throw new IllegalArgumentException("Response thid does not match request id");
		}
		if (!CollectionUtils.emptyIfNull(request.getTo()).contains(response.getFrom())) {
			throw new IllegalArgumentException("Response from not among request to");
		}
		if (!CollectionUtils.emptyIfNull(response.getTo()).contains(request.getFrom())) {
			throw new IllegalArgumentException("Request from not among response to");
		}
		if (!ignoreVP) {
			ResultSet resultSet = response.getBody().getResponse();
			VerifiablePresentation vp = request.getBody().getVp();
			Set<String> queries = vp.getVerifiableCredentials().stream() //
					.map(ValidatedQueryCredential::fromJsonLDObject) //
					.map(ValidatedQueryCredential::getIdentifier) //
					.collect(Collectors.toSet());

			for (Result r : CollectionUtils.emptyIfNull(resultSet.getResultset())) {
				if (!queries.contains(r.getQueryId())) {
					throw new IllegalArgumentException("Result query id does not match any request query id");
				}
			}
		}

	}

	public void validate(Request request, nl.kik.commons.datastation.dto.didcomm.Error response) {
		if (!Objects.equals(request.getId(), response.getPthid())) {
			throw new IllegalArgumentException("Response thid does not match request id");
		}
		if (!CollectionUtils.emptyIfNull(request.getTo()).contains(response.getFrom())) {
			throw new IllegalArgumentException("Response from not among request to");
		}
		if (!CollectionUtils.emptyIfNull(response.getTo()).contains(request.getFrom())) {
			throw new IllegalArgumentException("Request from not among response to");
		}
	}

	public void validate(nl.kik.commons.datastation.dto.didcomm.Error response) {
		ProblemReport body = response.getBody();
		if (body == null) {
			throw new IllegalArgumentException("Expected error body");
		}
		Code code = body.getCode();
		if (code == null) {
			throw new IllegalArgumentException("Error report is missing code");
		}
		if (!Sorter.Error.equals(code.getSorter())) {
			throw new IllegalArgumentException("Expected sorter error");
		}
		if (!Scope.Protocol.equals(code.getScope())) {
			throw new IllegalArgumentException("Expected protocol scope");
		}
	}

	public void validate(Request request) throws Exception {
		QueryRequest body = request.getBody();
		if (body == null) {
			throw new IllegalArgumentException("Expected request body");
		}
		if (!ignoreVP) {
			VerifiablePresentation vp = body.getVp();
			if (vp == null) {
				throw new IllegalArgumentException("VP is missing");
			}
			crypto.check(vp);
			if (vp.getHolder() == null) {
				throw new IllegalArgumentException("VP holder is empty");
			}
			if (!vp.getHolder().equals(request.getFrom())) {
				throw new IllegalArgumentException("VP is not from message sender");
			}
			vp.getVerifiableCredentials().forEach(c -> {
				if (!vp.getHolder().equals(c.getCredentialSubject().getId())) {
					throw new IllegalArgumentException("VC id does not match VP holder");
				}
			});
		}
	}

	public void validate(Response response) {
		QueryResponse body = response.getBody();
		if (body == null) {
			throw new IllegalArgumentException("Expected response body");
		}
		ResultSet resultSet = body.getResponse();
		if (resultSet == null) {
			throw new IllegalArgumentException("Expected response");
		}
		String id = response.getThid().toString();
		for (Result r : CollectionUtils.emptyIfNull(resultSet.getResultset())) {
			if (!id.equals(r.getMessageId())) {
				throw new IllegalArgumentException("Result message id does not match response thid");
			}
			if (StringUtils.isBlank(r.getQueryId())) {
				throw new IllegalArgumentException("Result missing query id");
			}
			if (r.getResult() == null) {
				throw new IllegalArgumentException("Result missing");
			}
		}
	}

	public void validateFull(JWT oauth, Message<?> m) throws Exception {
		validate(oauth, "didcomm-service-kikv");
		validate(m);
		validate(m, oauth);
		if (m instanceof Request r) {
			validate(r);
		}
		if (m instanceof Response r) {
			validate(r);
		}
		if (m instanceof nl.kik.commons.datastation.dto.didcomm.Error e) {
			validate(e);
		}
	}

	public void validateFull(JWT oauth, Request request, Message<?> m) throws Exception {
		validateFull(oauth, m);
		if (m instanceof Request r) {
			throw new IllegalArgumentException("Received request in response to request");
		}
		if (m instanceof Response r) {
			validate(request, r);
		}
		if (m instanceof nl.kik.commons.datastation.dto.didcomm.Error e) {
			validate(request, e);
		}
	}

}
