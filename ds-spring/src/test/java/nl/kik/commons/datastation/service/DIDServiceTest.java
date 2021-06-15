package nl.kik.commons.datastation.service;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JOSEException;

import uniresolver.ResolutionException;

class DIDServiceTest {
	private DIDService service;

	@BeforeEach
	void setUp() throws Exception {
		service = new DIDService();
		service.init();
	}

	@Test
	void testOk() throws JOSEException, ResolutionException, ParseException {
		service.getVerifier("did:web:did.actor:alice", "z6MkrmNwty5ajKtFqc1U48oL2MMLjWjartwc5sf2AihZwXDN");
		service.getVerifier("did:web:did.actor:alice",
				"did:web:did.actor:alice#z6MkrmNwty5ajKtFqc1U48oL2MMLjWjartwc5sf2AihZwXDN");
		service.getVerifier("did:web:did.actor:alice", null);
	}

	@Test
	void testNotFound() throws JOSEException, ResolutionException {
		assertThrows(JOSEException.class, () -> service.getVerifier("did:web:did.actor:britney", null));
	}

	@Test
	void testInvalidKey() throws JOSEException, ResolutionException {
		assertThrows(JOSEException.class, () -> service.getVerifier("did:web:did.actor:alice", "farts"));
	}

}
